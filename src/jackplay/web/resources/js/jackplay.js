//<!--input id='playGround' name='playGround' id='playGround' size="60" placeholder="E.g. com.example.RegistrationService" title='Format: className.methodName'/-->
//import {App} from 'auto-class-lookup';

// https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
function escapeRegexCharacters(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getSuggestions(allTargets, inputValue) {
  const escapedValue = escapeRegexCharacters(inputValue.trim());

  if (escapedValue === '') {
    return [];
  }

  const regex = new RegExp(escapedValue, 'i');

  return allTargets.filter(entry => regex.test(entry.targetName));
}

function getSuggestionValue(suggestion) {
  return suggestion.targetName;
}

let useShortTypeName = false;
function getShortTypeName(type) {
  let standardPackage = 'java.lang.'
  if (type.startsWith(standardPackage)) {
    return type.substring(standardPackage.length);
  } else {
    return type;
  }
}

function getSearchTerms(search, realClassName) {
  const defaultTerms = {classTerm: '', methodTerm: ''};

  if (!search) {
    return defaultTerms;
  } else if (search.indexOf('.') < 0) {     // no dot               e.g. mya or mya(
    if (search.indexOf('(') < 0) {          // no dot, no (         e.g. mya
      return {classTerm: search, methodTerm: search}
    } else {                                // no dot, ( found      e.g. mya(
      let idx = search.indexOf('(');
      let methodName = search.substring(0, idx);
      return { classTerm: '', methodTerm: methodName };
    }
  } else if (search.endsWith('.')) {        // ends with .          e.g. myapp. myapp.abc.
    return {classTerm: search.substring(0, search.length - 1), methodTerm: ''}
  } else if (search.indexOf('(') < 0) {     // no (, . in the middle e.g. myapp.Gr  or myapp.myapp2.my
    let lastDot = search.lastIndexOf('.');
    let lastPart = search.substring(lastDot + 1);
    let thePartBefore = search.substring(0, lastDot);

    return {classTerm: realClassName ? ((realClassName.toUpperCase().indexOf(search.toUpperCase()) >= 0) ? search
                                                                                                         : thePartBefore )
                                     : '',
            methodTerm: lastPart}
  } else if (search.indexOf('(') > 0) {     // with (, with . in the middle -> myapp.Greet.main(  or myapp.Greet.main(int
    let startParen = search.indexOf('(');
    let classAndMethod = search.substring(0, startParen);
    let lastDotBeforeParen = classAndMethod.lastIndexOf('.');
    let className = classAndMethod.substring(0, lastDotBeforeParen);
    let methodName = classAndMethod.substring(lastDotBeforeParen + 1, startParen);
    return {
      classTerm: className,
      methodTerm: methodName
    }
  } else {
    return defaultTerms;
  }
}

function highlightTermsInText(term, text) {
  const matches = AutosuggestHighlight.match(text, term);
  const parts = AutosuggestHighlight.parse(text, matches);

  return (
    <span>
    {
      parts.map((part, index) => {
        const className = part.highlight ? 'highlight' : null;

        return (
          <span className={className} key={index}>{part.text}</span>
        );
      })
    }
    </span>
  )
}

function highlightClassName(search, className) {
  let terms = getSearchTerms(search, className);
  return highlightTermsInText(terms.classTerm, className);
}

function highlightMethodName(search, methodName) {
  let terms = getSearchTerms(search);
  return highlightTermsInText(terms.methodTerm, methodName);
}

function renderSuggestion(suggestion, {value, valueBeforeUpDown}) {
  let startParen = suggestion.targetName.indexOf('(');
  let classAndMethod = suggestion.targetName.substring(0, startParen);
  let lastDotBeforeParen = classAndMethod.lastIndexOf('.');
  let className = classAndMethod.substring(0, lastDotBeforeParen);
  let methodName = classAndMethod.substring(lastDotBeforeParen + 1, startParen);
  let methodArgsList = suggestion.targetName.substring(startParen + 1, suggestion.targetName.length - 1);
  if (methodArgsList) {
    methodArgsList = methodArgsList.split(',').map(argType => useShortTypeName ? getShortTypeName(argType) : argType).join(', ');
  }
  const query = (valueBeforeUpDown || value).trim();

  return (
    <span>
      <span className='suggestion_classname'>{highlightClassName(query, className)}.</span>
      <span className='suggestion_method_name'>{highlightMethodName(query, methodName)}</span>
      <span className='suggestion_method_signature'>
          <span className='suggestion_method_paren'>(</span>
          <span className='suggestion_method_args_list'>{methodArgsList}</span>
          <span className='suggestion_method_paren'>)</span>
      </span>
    </span>
  );
}

class AutoClassLookup extends React.Component { // eslint-disable-line no-undef
  constructor() {
    super();

    this.state = {
      value: '',
      suggestions: getSuggestions([], '')
    };

    this.onChange = this.onChange.bind(this);
    this.onSuggestionsUpdateRequested = this.onSuggestionsUpdateRequested.bind(this);
  }

  onChange(event, { newValue }) {
    this.setState({
      value: newValue
    });
  }

  onSuggestionsUpdateRequested({ value }) {
    this.setState({
      suggestions: getSuggestions(this.props.loadedTargets, value)
    });
  }

  render() {
    const { value, suggestions } = this.state;
    const inputProps = {
      placeholder: 'Type a class or method name, e.g. com.abc.UserService.getUser',
      value,
      onChange: this.onChange
    };

    return (
      <Autosuggest suggestions={suggestions} // eslint-disable-line react/jsx-no-undef
                   onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                   getSuggestionValue={getSuggestionValue}
                   renderSuggestion={renderSuggestion}
                   inputProps={inputProps}
                   loadedTargets={this.props.loadedTargets}/>
    );
  }
}

let MethodRedefine = React.createClass({
  emptyMethodSource: function() {
    document.getElementById('newSource').value = '';
  },
  render: function() {
  return (
      <div style={{display: this.props.show, marginLeft: '6px'}}>
        <div><label htmlFor='newSource'>Write method source:</label></div>
        <div>
          <textarea rows="8" cols="66" id="newSource" placeholder="{ return 10; }" className='code'></textarea>
        </div>
        <div><button onClick={this.props.submit} title='submit new method source'>Submit</button>
             <button onClick={this.emptyMethodSource} title='clear input area'>Empty</button>
             <button onClick={this.props.cancel} title='hide this panel'>Cancel</button>
             <span className="tooltip "> An Example
                <span className="tooltipBelow tooltiptext code " style={{width: '520px', fontSize: '13px', marginLeft: '-82px'}}>
                    <pre><code>{
                     " {\n  java.util.Calendar rightNow = java.util.Calendar.getInstance();\n" +
                     "  return rightNow.get(java.util.Calendar.SECOND); \n" + " }"
                     }</code></pre>
                </span>
             </span>
             <span className="tooltip "> Limitation
                <span className="tooltipBelow tooltiptext " style={{width: '460px', marginLeft: '-75px'}}>
                  <ul>
                    <li>Use full classname (except java.lang): e.g. java.util.Calendar</li>
                    <li>... see <a href='https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#limit'>Javassist</a> </li>
                  </ul>
                </span>
             </span>
        </div>
      </div>
  )}
});


let PlayPanel = React.createClass({
  getInitialState: function() {
    return {showMethodDefine: 'none'};
  },
  toggleMethodRedefine: function() {
    this.setState(Object.assign(this.state, {showMethodDefine: 'none' == this.state.showMethodDefine ? '' : 'none'}));
  },
  cancelMethodRedefine: function() {
    this.setState(Object.assign(this.state, {showMethodDefine: 'none'}));
  },
  submitMethodTrace: function() {
    let v = $("div#content input[type=text]")[0].value.trim();

    if (v) {
        $.ajax({
          url: '/logMethod',
          data: 'playGround=' + v
        });
    };
  },
  submitMethodRedefine: function() {
    let pg = $("div#content input[type=text]")[0].value.trim();
    let src = document.getElementById('newSource').value.trim();

    if (pg && src) {
        $.ajax({
          method: 'post',
          url: '/redefineMethod',
          contentType: "application/x-www-form-urlencoded",
          data: 'playGround=' + pg + "&newSource=" + encodeURIComponent(src)
        });
    }
  },
  requestToClearLogHistory: function() {
    $.ajax({
          url: '/clearLogHistory',
    });
    this.props.clearLogHistory();
  },
  render: function() {
    return (
    <div>
      <table>
        <tr>
          <td>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} />
          </td>
          <td><button onClick={this.submitMethodTrace} title='trace this method'>Trace</button>
              <button onClick={this.toggleMethodRedefine} title='show/hide method redefinition panel'>Redefine</button>
              <button onClick={this.requestToClearLogHistory} title='clear trace log'>Clear</button></td>
          <td>
            <div className='checkboxSwitch' title='Switch data sync'>
              <input id='autoSync' type="checkbox" defaultChecked='true' onChange={this.props.toggleDataSync}/>
              <label htmlFor='autoSync'></label>
            </div>
          </td>
        </tr>
      </table>
      <MethodRedefine show={this.state.showMethodDefine} cancel={this.cancelMethodRedefine} submit={this.submitMethodRedefine}/>
    </div>
    );
  }
});

let LogHistory = React.createClass({
  render: function() {
    let logList = this.props.logHistory.map(function(entry) {
      return (
       <div>
         <span title={entry.type}>{entry.when}</span>
         <span> | </span>
         <span title={entry.type} className={entry.type}>{entry.log}</span>
       </div>
      );
    });
    return (
      <div className='logHistoryContainer'>
        {logList}
      </div>
    );
  }
});

let JackPlay = React.createClass({
  getInitialState: function() {
    return {logHistory: [],
            loadedTargets: [],
            isSyncWithServerPaused: false};
  },
  componentDidMount: function() {
    this.syncDataWithServer();
    setInterval(this.checkDataSync, 2218);
  },
  syncDataWithServer: function() {
    $.ajax({
      url: '/logHistory',
      success: function(history) {
        this.setState(Object.assign(this.state, {logHistory: history}));
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
    $.ajax({
      url: '/loadedTargets',
      success: function(targets) {
        this.setState(Object.assign(this.state, {loadedTargets: targets}));
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
  },
  checkDataSync: function() {
    if (!this.state.isSyncWithServerPaused) this.syncDataWithServer();
  },
  clearLogHistory: function() {
    this.setState(Object.assign(this.state, {logHistory: []}));
  },
  toggleDataSync: function() {
    this.setState(Object.assign(this.state, {isSyncWithServerPaused: !this.state.isSyncWithServerPaused}));
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.syncDataWithServer}
                 clearLogHistory={this.clearLogHistory}
                 toggleDataSync={this.toggleDataSync}
                 loadedTargets={this.state.loadedTargets}/>
      <LogHistory logHistory={this.state.logHistory}
                  historyLoader={this.syncDataWithServer}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);