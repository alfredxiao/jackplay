//import {App} from 'auto-class-lookup';

// https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters

class Modal extends React.Component{

  constructor(props){
    super()
    this.hideOnOuterClick = this.hideOnOuterClick.bind(this)
    this.fadeIn = this.fadeIn.bind(this)
    this.fadeOut = this.fadeOut.bind(this)

    let opacity = 0,
      display = 'block',
      visibility = 'hidden';

    if(props.show){
      opacity = 1;
      display = 'block';
      visibility = 'visible'
    }

    this.state = {
      opacity,
      display,
      visibility,
      show: props.show
    };

  }

  hideOnOuterClick(event){
    if(this.props.closeOnOuterClick === false) return
    if(event.target.dataset.modal) this.props.onClose(event)
  }

  componentWillReceiveProps(props){
    if(this.props.show != props.show){
      if(this.props.transitionSpeed){
        if(props.show == true) this.fadeIn()
        else this.fadeOut()
      }
      else this.setState({show: props.show})
    }
  }

  fadeIn(){
    this.setState({
      display: 'block',
      visibility: 'visible',
      show: true
    }, ()=>{
      setTimeout(()=>{
        this.setState({opacity: 1})
      },10)
    })
  }

  fadeOut(){
    this.setState({opacity: 0}, ()=>{
      setTimeout(()=>{
        this.setState({show: false})
      }, this.props.transitionSpeed)
    })
  }

  render(){
    if(!this.state.show) return null
    let modalStyle, containerStyle
    //completely overwrite if they use a class
    if(this.props.className){
      modalStyle = this.props.style
      containerStyle = this.props.containerStyle
    }
    else{
      modalStyle = Object.assign({}, styles.modal, this.props.style)
      containerStyle = Object.assign({}, styles.container, this.props.containerStyle)
    }
    if(this.props.transitionSpeed) modalStyle = Object.assign({}, this.state, modalStyle)

    return (
      <div {...this.props} style={modalStyle} onClick={this.hideOnOuterClick} data-modal="true">
        <div className={this.props.containerClassName} style={containerStyle}>
          {this.props.children}
        </div>
      </div>
    )
  }
}
const ERROR = 'ERROR';
const INFO = 'INFO';
const SUNG= '\u266A';
const dTriangle = '\u25BE';
const uTriangle = '\u25B4';
const CROSS = '\u2717';
const STAR = '\u2605';
const TRACE_MODE = 'TRACE';
const REDEFINE_MODE = 'REDEFINE';
const CONTROL = 'CONTROL';
const TRACE_OR_REDEFINE = 'PLAY{TRACE, REDEFINE}';

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

let modalDefaultStyle = {
  position: 'fixed',
  fontFamily: 'Arial, Helvetica, sans-serif',
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  background: 'rgba(0, 0, 0, 0.8)',
  zIndex: 99999,
  transition: 'opacity 1s ease-in',
  pointerEvents: 'auto',
  overflowY: 'auto'
}

let containerDefaultStyle = {
  width: '680px',
  position: 'relative',
  margin: '6% auto',
  padding: '10px 20px 13px 20px',
  background: '#fff'
}

let closeDefaultStyle = {
  background: '#606061',
  color: '#FFFFFF',
  lineHeight: '25px',
  position: 'absolute',
  right: '-12px',
  textAlign: 'center',
  top: '-10px',
  width: '24px',
  textDecoration: 'none',
  fontWeight: 'bold',
  borderRadius: '12px',
  boxShadow: '1px 1px 3px #000',
  cursor: 'pointer'
}

let PlayBook = React.createClass({
  render: function(){
    let program = this.props.program;
    let programList = Object.keys(program).map(function (genre) {
      let classList = Object.keys(program[genre]).map(function (clsName) {
        let methodList = Object.keys(program[genre][clsName]).map(function (methodName) {
          return (
            <div>
              <span>{methodName}</span>
            </div>
          )
        });
        return (
           <div>
             <div>{clsName}</div>
             <div style={{marginLeft: '10px'}}>{methodList}</div>
           </div>
        )
      });
      return (
           <div>
             <div>{genre}</div>
             <div style={{marginLeft: '10px'}}>{classList}</div>
           </div>
      )
    });
    return (
      <div>
        <Modal className="test-class" //this will completely overwrite the default css completely
              style={modalDefaultStyle} //overwrites the default background
              containerStyle={containerDefaultStyle} //changes styling on the inner content area
              containerClassName="test"
              closeOnOuterClick={false}
              show={this.props.playBookBeingShown}
              >

          <a style={closeDefaultStyle} onClick={this.props.hidePlayBook}>X</a>
          <div style={{display: this.props.show}}>
              <div style={{overflowX: 'auto'}}>
                {programList}
              </div>
          </div>
        </Modal>
      </div>
    )
  }
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
        <div style={{display:'inline', paddingLeft: '5px'}}>
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
    return {playMode: TRACE_MODE,
            playBookBeingShown: false};
  },
  toggledLabel: function() {
    return TRACE_MODE == this.state.playMode ? dTriangle : uTriangle;
  },
  togglePlayMode: function() {
    this.setState(Object.assign(this.state, {playMode: TRACE_MODE == this.state.playMode ? REDEFINE_MODE : TRACE_MODE}));
  },
  showPlayBook: function() {
    this.setState(Object.assign(this.state, {playBookBeingShown: true}));
  },
  hidePlayBook: function() {
    this.setState(Object.assign(this.state, {playBookBeingShown: false}));
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
            this.togglePlayMode();
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
                     (<button onClick={this.submitMethodTrace} title='trace this method'>Trace</button>)
                     :
                     (<button onClick={this.submitMethodRedefine} title='submit new method source'>Redefine</button>);
    return (
    <div>
            <button style={{borderRight: 0, margin: 0, paddingLeft: '6px', width: '20px', borderRadius: '4px 0px 0px 4px', outline:'none'}}
                    id='searchIcon'>
                <span className="fa fa-search" style={{fontSize:'14px', color: '#666'}}></span>
            </button>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} />
            <button onClick={this.togglePlayMode} title='show/hide method redefinition panel'
                    style={{borderLeft: 0, margin: 0, width: '20px', borderRadius: '0px 4px 4px 0px', outline:'none'}}>{this.toggledLabel()}</button>
            {playButton}
            <button onClick={this.showPlayBook} title='show/hide information about method being traced'>Control</button>
            <LogControl updateFilter={this.props.updateFilter}
                        clearFilter={this.props.clearFilter}
                        toggleDataSync={this.props.toggleDataSync}
                        clearLogHistory={this.props.clearLogHistory} />
            <MethodRedefine show={this.state.playMode == REDEFINE_MODE ? '' : 'none'}/>
            <PlayBook playBookBeingShown={this.state.playBookBeingShown}
                      hidePlayBook={this.hidePlayBook}
                      program={this.props.program}/>
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
            program: [],
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
      url: '/program',
      success: function(program) {
        this.setState(Object.assign(this.state, {program: program}));
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
                 program={this.state.program}
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