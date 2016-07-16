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
  render: function() {
  return (
      <div style={{display: this.props.show, marginLeft: '0px'}}>
        <div>
          <textarea rows="8" id="newSource" placeholder="{ return 10; }" className='code' style={{marginTop: '-1px', marginLeft: '0px', width: '565px'}}></textarea>
        </div>
        <div>
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

const dTriangle = '\u25BE';
const uTriangle = '\u25B4';
const TRACE_MODE = 'TRACE';
const REDEFINE_MODE = 'REDEFINE';
let PlayPanel = React.createClass({
  getInitialState: function() {
    return {playMode: TRACE_MODE};
  },
  toggledLabel: function() {
    return TRACE_MODE == this.state.playMode ? dTriangle : uTriangle;
  },
  toggleMethodRedefine: function() {
    this.setState(Object.assign(this.state, {playMode: TRACE_MODE == this.state.playMode ? REDEFINE_MODE : TRACE_MODE}));
  },
  showMethodDefine: function() {
    return this.state.playMode == MethodRedefine;
  },
  submitMethodTrace: function() {
    let v = $("div#content input[type=text]")[0].value.trim();

    if (v) {
      this.props.setTraceStarted(true);
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
  render: function() {
    let playButton = (TRACE_MODE == this.state.playMode) ?
                     (<button onClick={this.submitMethodTrace} title='trace this method' style={{marginLeft: '5px'}}>Trace</button>)
                     :
                     (<button onClick={this.submitMethodRedefine} title='submit new method source' style={{marginLeft: '5px'}}>Redefine</button>);
    return (
    <div>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} />
            <button onClick={this.toggleMethodRedefine} title='show/hide method redefinition panel'
                    style={{borderLeft: 0, margin: 0, width: '20px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{this.toggledLabel()}</button>
            {playButton}
            <MethodRedefine show={this.state.playMode == REDEFINE_MODE ? '' : 'none'}/>
    </div>
    );
  }
});

let LogHistory = React.createClass({
  getInitialState: function() {
    return {filter: ''};
  },
  requestToClearLogHistory: function() {
    $.ajax({
          url: '/clearLogHistory',
    });
    this.props.clearLogHistory();
  },
  updateFilter: function() {
    this.setState(Object.assign(this.state, {filter: document.getElementById('logFilter').value.trim()}))
  },
  clearFilter: function() {
    document.getElementById('logFilter').value = '';
    this.updateFilter();
  },
  render: function() {
    if (!this.props.traceStarted) {
      return null;
    }

    let filter = this.state.filter;
    let regex = new RegExp(filter, 'i');
    let logList = this.props.logHistory.map(function(entry) {
        if (!filter || regex.test(entry.log)) {
          return (
               <div>
                 <span title={entry.type}>{entry.when}</span>
                 <span> | </span>
                 <span title={entry.type} className={entry.type}>{highlightTermsInText(filter, entry.log)}</span>
               </div>
          )
        } else {
          return null;
        };
    });
    const CROSS = '\u2717';
    return (
      <div className='logHistoryContainer'>
        <div>
          <input name='logFilter' id='logFilter' placeholder='filter logs' onChange={this.updateFilter}
                 style={{borderRadius: '4px 0px 0px 4px', borderRight: '0px', outline: 'none'}} />
          <button title='Clear filter' onClick={this.clearFilter}
                  style={{borderLeft: 0, margin: 0, width: '23px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{CROSS}</button>
          <button onClick={this.requestToClearLogHistory} title='clear trace log' style={{marginLeft: '5px'}}>Clear All</button>
          <div className='checkboxSwitch' title='Switch data sync' style={{display: 'inline'}}>
            <input id='autoSync' type="checkbox" defaultChecked='true' onChange={this.props.toggleDataSync}/>
            <label htmlFor='autoSync'></label>
          </div>
        </div>
        {logList}
      </div>
    );
  }
});

let JackPlay = React.createClass({
  getInitialState: function() {
    return {logHistory: [],
            loadedTargets: [],
            traceStarted: false,
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
        this.setState(Object.assign(this.state, {logHistory: history,
                                                 traceStarted: history.length > 0 || this.state.traceStarted }));
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
  setTraceStarted: function(v) {
    this.setState(Object.assign(this.state, {traceStarted: v}))
  },
  render: function() {
    return (
    <div>
      <PlayPanel loadedTargets={this.state.loadedTargets} setTraceStarted={this.setTraceStarted}/>
      <br/>
      <LogHistory logHistory={this.state.logHistory}
                  clearLogHistory={this.clearLogHistory}
                  traceStarted={this.state.traceStarted}
                  toggleDataSync={this.toggleDataSync}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);