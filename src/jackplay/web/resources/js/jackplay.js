//import {App} from 'auto-class-lookup';

// https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters

const ERROR = 'ERROR';
const INFO = 'INFO';
const SUNG= '\u266A';
const dTriangle = '\u25BE';
const uTriangle = '\u25B4';
const CROSS = '\u2717';
const STAR = '\u2605';
const TRACE_MODE = 'TRACE';
const REDEFINE_MODE = 'REDEFINE';

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
          <textarea rows="8" id="newSource" placeholder="{ return 10; }" className='code'
                    style={{marginTop: '-1px', marginLeft: '0px', width: '549px', outline: 'none'}}></textarea>
        </div>
        <div>
             <span className="tooltip "> An Example
                <span className="tooltipBelow tooltiptext code " style={{width: '520px', fontSize: '13px', marginLeft: '-82px'}}>
                    <pre><code>{
                     " {\n  java.util.Calendar now = java.util.Calendar.getInstance();\n" +
                     "  return now.get(java.util.Calendar.SECOND); \n" + " }"
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

let LogControl = React.createClass({
  requestToClearLogHistory: function() {
    $.ajax({
          url: '/clearLogHistory',
    });
    this.props.clearLogHistory();
  },
  render: function() {
    return (
        <div style={{display:'inline', paddingLeft: '15px'}}>
          <input name='logFilter' id='logFilter' placeholder='filter logs' onChange={this.props.updateFilter}
                 style={{borderRadius: '4px 0px 0px 4px', borderRight: '0px', outline: 'none', width: '133px'}} />
          <button title='Clear filter' onClick={this.props.clearFilter}
                  style={{borderLeft: 0, margin: 0, width: '23px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{CROSS}</button>
          <button onClick={this.requestToClearLogHistory} title='clear trace log' style={{marginLeft: '5px'}}>Clear All</button>
          <div className='checkboxSwitch' title='Switch data sync' style={{display: 'inline'}}>
            <input id='autoSync' type="checkbox" defaultChecked='true' onChange={this.props.toggleDataSync}/>
            <label htmlFor='autoSync'></label>
          </div>
        </div>
    )
  }
});

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
    let longMethodName = $("div#content input[type=text]")[0].value.trim();
    if (!longMethodName) this.props.setGlobalMessage(ERROR, 'Please type in a valid classname.methodname!');

    if (longMethodName) {
      this.props.setTraceStarted(true);
      $.ajax({
        url: '/logMethod',
        data: 'longMethodName=' + longMethodName,
        success: function(data) {
          this.props.setGlobalMessage(INFO, data);
        }.bind(this),
        error: function(data) {
          this.props.setGlobalMessage(ERROR, data.statusText + " : " + data.responseText);
        }.bind(this)
      });
    };
  },
  submitMethodRedefine: function() {
    let longMethodName = $("div#content input[type=text]")[0].value.trim();
    let src = document.getElementById('newSource').value.trim();

    if (!longMethodName || !src) this.props.setGlobalMessage(ERROR, 'A valid classname.methodname and source body must be provided!');

    if (longMethodName && src) {
        $.ajax({
          method: 'post',
          url: '/redefineMethod',
          contentType: "application/x-www-form-urlencoded",
          data: 'longMethodName=' + longMethodName + "&src=" + encodeURIComponent(src),
          success: function(data) {
            this.props.setGlobalMessage(INFO, data);
          }.bind(this),
          error: function(data) {
            this.props.setGlobalMessage(ERROR, data.statusText + " : " + data.responseText);
          }.bind(this)
        });
    }
  },
  render: function() {
    let playButton = (TRACE_MODE == this.state.playMode) ?
                     (<button onClick={this.submitMethodTrace} title='trace this method' style={{marginLeft: '5px', borderRadius: '4px 0px 0px 4px'}}>Trace</button>)
                     :
                     (<button onClick={this.submitMethodRedefine} title='submit new method source' style={{marginLeft: '5px', borderRadius: '4px 0px 0px 4px'}}>Redefine</button>);
    return (
    <div>
            <button style={{borderRight: 0, margin: 0, paddingLeft: '6px', width: '20px', borderRadius: '4px 0px 0px 4px', outline:'none'}}
                    id='searchIcon'>
                <span className="fa fa-search" style={{fontSize:'14px', color: '#666'}}></span>
            </button>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} />
            <button onClick={this.toggleMethodRedefine} title='show/hide method redefinition panel'
                    style={{borderLeft: 0, margin: 0, width: '20px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{this.toggledLabel()}</button>
            {playButton}
            <button title='show/hide information about method being traced' onClick={this.props.clearFilter}
                    style={{borderLeft: 0, margin: 0, width: '20px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{dTriangle}</button>
            <LogControl updateFilter={this.props.updateFilter}
                        clearFilter={this.props.clearFilter}
                        toggleDataSync={this.props.toggleDataSync}
                        clearLogHistory={this.props.clearLogHistory} />
            <MethodRedefine show={this.state.playMode == REDEFINE_MODE ? '' : 'none'}/>
    </div>
    );
  }
});

let LogHistory = React.createClass({
  render: function() {
    if (!this.props.traceStarted) {
      return null;
    }

    let filter = this.props.filter;
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
    return (
      <div className='logHistoryContainer'>
        {logList}
      </div>
    );
  }
});

let GlobalMessage = React.createClass({
  render: function() {
    let gm = this.props.globalMessage;
    if (gm) {
      let icon = (INFO == gm.level) ? SUNG : STAR;
      return (
        <div style={{paddingBottom: '8px'}}>
          <span className='globalMessage'>
              <span>
                <span style={{paddingRight: '5px'}}>{icon}</span>
                <span className={'msg_' + gm.level}>{gm.message}</span>
              </span>
          </span>
          <button onClick={this.props.clearGlobalMessage} className='light' title='Dismiss this message'>{CROSS}</button>
        </div>
      );
    }
   return null;
  }
});

let JackPlay = React.createClass({
  getInitialState: function() {
    return {logHistory: [],
            filter: '',
            loadedTargets: [],
            traceStarted: false,
            globalMessage: null,
            isSyncWithServerPaused: false};
  },
  componentDidMount: function() {
    this.syncDataWithServer();
    setInterval(this.checkDataSync, 3333);
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
  updateFilter: function() {
    this.setState(Object.assign(this.state, {filter: document.getElementById('logFilter').value.trim()}))
  },
  clearFilter: function() {
    document.getElementById('logFilter').value = '';
    this.updateFilter();
  },
  setGlobalMessage: function(level, msg) {
    this.setState(Object.assign(this.state, {globalMessage: {level: level, message: msg}}));
  },
  clearGlobalMessage: function() {
    this.setState(Object.assign(this.state, {globalMessage: null}))
  },
  render: function() {
    return (
    <div>
      <PlayPanel loadedTargets={this.state.loadedTargets}
                 setTraceStarted={this.setTraceStarted}
                 updateFilter={this.updateFilter}
                 clearFilter={this.clearFilter}
                 toggleDataSync={this.toggleDataSync}
                 setGlobalMessage={this.setGlobalMessage}
                 clearLogHistory={this.clearLogHistory} />
      <br/>
      <GlobalMessage globalMessage={this.state.globalMessage} clearGlobalMessage={this.clearGlobalMessage} />
      <LogHistory logHistory={this.state.logHistory}
                  traceStarted={this.state.traceStarted}
                  filter={this.state.filter}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);