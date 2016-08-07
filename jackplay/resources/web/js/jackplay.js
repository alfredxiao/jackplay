//import {App} from 'auto-class-lookup';
let alertGlobal = {};
let ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;

function formatNumber (num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,")
}

let toMessage = function(obj) {
  return JSON.stringify(obj);
}

let toErrorMessage = function(obj) {
  if (obj.responseText) {
    return obj.responseText;
  } else {
    return toMessage(obj);
  }
}

// the alert notification is based on http://schiehll.github.io/react-alert/
// highlight is based on https://github.com/moroshko/react-autosuggest
// modal dialog is based on https://github.com/sergiodxa/react-simple-modal

class AlertMessage extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      closeButtonStyle: {}
    };
  }
  /**
   * Handle the close button click
   * @return {void}
   */
  _handleCloseClick(){
    this._removeSelf();
  }
  /**
   * Include the given icon or use the default one
   * @return {React.Component}
   */
  _showIcon(){
    let icon = '';
    if(this.props.icon){
      icon = this.props.icon;
    }
    else{
      icon = <div className={this.props.type + '-icon'} />;
    }

    return icon;
  }
  /**
   * Remove the alert after the given time
   * @return {void}
   */
  _countdown(){
    setTimeout(() => {
      this._removeSelf();
    }, this.props.time);
  }
  /**
   * Emit a event to AlertContainer remove this alert from page
   * @return {void}
   */
  _removeSelf(){
    alertGlobal.reactAlertEvents.emit('ALERT.REMOVE', this);
  }

  componentDidMount(){
    this.domNode = ReactDOM.findDOMNode(this);
    this.setState({
      closeButtonStyle: {
        height: this.domNode.offsetHeight + 'px',
        lineHeight: this.domNode.offsetHeight + 'px'
      }
    });

    if(this.props.time > 0){
      this._countdown();
    }
  }

  render(){
    return(
      <div className='alertContainer'>
        <div className='alertNoticeIcon'>
          {this._showIcon.bind(this)()}
        </div>
        <div className='alertMessage'>
          {this.props.message}
        </div>
        <div onClick={this._handleCloseClick.bind(this)} style={{}} className='alertCloseIcon'>
          {false ? "XX": <span className="fa fa-times" aria-hidden="true"></span>}
        </div>
      </div>
    );
  }
}

AlertMessage.defaultProps = {
  icon: '',
  message: '',
  type: 'info'
}

AlertMessage.propTypes = {
  type: React.PropTypes.oneOf(['info', 'success', 'error'])
}

class AlertContainer extends React.Component {
  constructor(props){
    super(props);
    alertGlobal.reactAlertEvents = new EventEmitter();
    this.state = {
      alerts: []
    };
    this.style = this._setStyle();
    this.theme = this._setTheme();
    this._eventListners();
  }
  /**
   * Show the alert in the page with success type
   * @param  {string} message
   * @param  {Object} options
   * @return {void}
   */
  success(message, options = {}){
    options.type = 'success';
    this.show(message, options);
  }
  /**
   * Show the alert in the page with error type
   * @param  {string} message
   * @param  {Object} options
   * @return {void}
   */
  error(message, options = {}){
    options.type = 'error';
    this.show(message, options);
  }
  /**
   * Show the alert in the page with info type
   * @param  {string} message
   * @param  {Object} options
   * @return {void}
   */
  info(message, options = {}){
    options.type = 'info';
    this.show(message, options);
  }
  /**
   * Show the alert in the page
   * @param  {string} message
   * @param  {Object} options
   * @return {void}
   */
  show(message, options = {}){
    let alert = {};
    alert.message = message;
    alert = Object.assign(alert, options);
    this.setState({alerts: this._addAlert(alert)});
  }
  /**
   * Remove all tasks from the page
   * @return {void}
   */
  removeAll(){
    this.setState({alerts: []});
  }
  /**
   * Add an alert
   * @param {Object} alert
   */
  _addAlert(alert){
    alert.uniqueKey = this._genUniqueKey();
    alert.style = this.theme;
    if(!alert.hasOwnProperty('time')){
      alert.time = this.props.time;
    };
    alert.closeIconClass = 'close-' + this.props.theme;
    this.state.alerts.push(alert);
    return this.state.alerts;
  }
  /**
   * Generate a key
   * @return {string}
   */
  _genUniqueKey(){
    return new Date().getTime().toString() + Math.random().toString(36).substr(2, 5);
  }
  /**
   * Remove an AlertMessage from the container
   * @param  {AlertMessage} alert
   * @return {void}
   */
  _removeAlert(alert){
    return this.state.alerts.filter((a) => {
      return a.uniqueKey != alert.props.uniqueKey;
    });
  }
  /**
   * Listen to alert events
   * @return {void}
   */
  _eventListners(){
    alertGlobal.reactAlertEvents.on('ALERT.REMOVE', (alert) => {
      this.setState({alerts: this._removeAlert(alert)});
    });
  }
  /**
   * Set the alert position on the page
   */
  _setStyle(){
    let position = {};
    switch(this.props.position){
      case 'top left':
        position = {
          top: 0,
          right: 'auto',
          bottom: 'auto',
          left: 0
        }
        break;
      case 'top right':
        position = {
          top: 0,
          right: 0,
          bottom: 'auto',
          left: 'auto'
        }
        break;
      case 'bottom left':
        position = {
          top: 'auto',
          right: 'auto',
          bottom: 0,
          left: 0
        }
        break;
      default:
        position = {
          top: 'auto',
          right: 0,
          bottom: 0,
          left: 'auto'
        }
        break;
    }

    return {
      margin: this.props.offset + 'px',
      top: position.top,
      right: position.right,
      bottom: position.bottom,
      left: position.left,
      position: 'absolute',
      zIndex: 99999
    };
  }
  /**
   * Set the style of the alert based on the chosen theme
   */
  _setTheme(){
    let theme = {};
    switch(this.props.theme){
      case 'light':
        theme = {
          alert: {
            backgroundColor: '#fff',
            color: '#333'
          },
          closeButton: {
            bg: '#f3f3f3'
          }
        }
        break;
      default:
        theme = {
          alert: {
            backgroundColor: '#333',
            color: '#fff'
          },
          closeButton: {
            bg: '#444'
          }
        }
        break;
    }

    return theme;
  }

  componentDidUpdate(){
    this.style = this._setStyle();
    this.theme = this._setTheme();
  }

  render(){
    return(
      <div style={this.style} className="react-alerts">
        <ReactCSSTransitionGroup
          transitionName={this.props.transition}
          transitionEnterTimeout={250}
          transitionLeaveTimeout={250}>
          {this.state.alerts.map((alert, index) => {
            return <AlertMessage key={alert.uniqueKey} {...alert} />
          })}
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

AlertContainer.defaultProps = {
  offset: 14,
  position: 'bottom left',
  theme: 'dark',
  time: 5000,
  transition: 'scale'
}

AlertContainer.propTypes = {
  offset: React.PropTypes.number,
  position: React.PropTypes.oneOf([
    'bottom left',
    'bottom right',
    'top right',
    'top left',
  ]),
  theme: React.PropTypes.oneOf(['dark', 'light']),
  time: React.PropTypes.number,
  transition: React.PropTypes.oneOf(['scale', 'fade'])
}



class App extends React.Component {
  constructor(props){
    super(props);
    this.alertOptions = {
      offset: 14,
      position: 'bottom left',
      theme: 'dark',
      time: 5000,
      transition: 'scale'
    };
  }

  showAlert(){
    this.msg.show('my message....', {
      time: 3000,
      type: 'success',
      icon: <span className="fa fa-info-circle" style={{fontSize:'22px', color: '#666'}} aria-hidden="true"></span>
    });
  }

  render(){
    return(
      <div>
        <AlertContainer ref={a => this.msg = a} {...this.alertOptions} />
        <button onClick={this.showAlert.bind(this)}>Show Alert</button>
      </div>
    );
  }
}

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
const BULLET = '\u2022';
const dTriangle = '\u25BE';
const uTriangle = '\u25B4';
const CROSS = '\u2717';
const STAR = '\u2605';
const RETURNS_ARROW = '\u27F9';
const THROWS_ARROW = '\u27FF';
const TRACE_MODE = 'TRACE';
const REDEFINE_MODE = 'REDEFINE';
const CONTROL = 'CONTROL';
const TRACE_OR_REDEFINE = 'PLAY{TRACE, REDEFINE}';
const METHOD_LOGGING = 'METHOD_LOGGING';
const SHOW_MAX_HIT_SEARCH = 25;
const TRIGGER_POINT_ENTER = 'MethodEntry';
const TRIGGER_POINT_RETURNS = 'MethodReturns';
const TRIGGER_POINT_THROWS_EXCEPTION = 'MethodThrowsException';

function escapeRegexCharacters(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getSuggestions(allTargets, inputValue) {
  const escapedValue = escapeRegexCharacters(inputValue.trim());

  if (escapedValue === '') {
    return [];
  }

  const regex = new RegExp(escapedValue, 'i');

  let hitByMethodLongName = Lazy(allTargets).filter(entry => regex.test(entry.methodLongName)).take(SHOW_MAX_HIT_SEARCH).toArray();
  let hitByMethodFullName = Lazy(allTargets).filter(entry => regex.test(entry.methodFullName)).take(SHOW_MAX_HIT_SEARCH);
  return hitByMethodLongName.length > 0 ?  hitByMethodLongName : hitByMethodFullName.toArray();
}

function getSuggestionValue(suggestion) {
  return suggestion.methodFullName;
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

function extractMethodInfo(methodFullName) {
  let startParen = methodFullName.indexOf('(');
  let classAndMethod = methodFullName.substring(0, startParen);
  let lastDotBeforeParen = classAndMethod.lastIndexOf('.');
  let className = classAndMethod.substring(0, lastDotBeforeParen);
  let methodName = classAndMethod.substring(lastDotBeforeParen + 1, startParen);
  let methodArgsList = methodFullName.substring(startParen + 1, methodFullName.length - 1);

  return {
    className: className,
    methodName: methodName,
    methodArgsList: methodArgsList
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
    let methodInfo = extractMethodInfo(search);
    return {
      classTerm: methodInfo.className,
      methodTerm: methodInfo.methodName
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


function renderSuggestion(suggestion, {value, valueBeforeUpDown}) {
  const query = (valueBeforeUpDown || value).trim();
  let methodFullName = suggestion.methodFullName;

  let queryStart = methodFullName.toLowerCase().indexOf(query.toLowerCase());
  if (queryStart < 0) return null;

  let methodInfo = extractMethodInfo(suggestion.methodFullName);
  let className = methodInfo.className;
  let methodName = methodInfo.methodName;
  let methodArgsList = methodInfo.methodArgsList;
  if (methodArgsList) {
    methodArgsList = methodArgsList.split(',').map(argType => useShortTypeName ? getShortTypeName(argType) : argType).join(', ');
  }

  let styleArray = new Array(methodFullName.length);
  for (let i = 0; i<styleArray.length; i++) styleArray[i] = 0;

  // index in below array is inclusive
  let segmentsIndexArray = [{start: 0,
                             end: className.length,
                             style: 1},                                 // this covers abc.MyService.
                            {start: className.length + 1,
                             end: className.length + methodName.length, // this covers myfunc1
                             style: 2},
                            {start: className.length + methodName.length + 1,
                             end: methodFullName.length - 1,            // this covers (int,boolean)
                             style: 4},
                            {start: queryStart,
                             end: queryStart + query.length - 1,
                             style: 8}];
  // set the style array with those segments
  //    abc.MyService. | func1 | (java.lang.String)
  //    11111111111111   22222   444444444444444444
  //                88   88                         // if search query is: `e.fu`
  for (let i1=0; i1<segmentsIndexArray.length; i1++) {
    let segIndex = segmentsIndexArray[i1];
    for (let i2=segIndex.start; i2<=segIndex.end; i2++) {
      styleArray[i2] += segIndex.style;
    }
  }

  // time to collect segments.
  var segments = new Array();
  const NO_PREVIOUS_SEG = 'NO_PREVIOUS_SEG';
  const HAS_PREVIOUS_SEG = 'HAS_PREVIOUS_SEG';
  let status = NO_PREVIOUS_SEG;
  let currentSegmentIndex = -1;
  var previousStyle = -1;
  let createNewSeg = function (i) {
        currentSegmentIndex++;
        segments[currentSegmentIndex] = {start: i, style: styleArray[i]};
        previousStyle = styleArray[i];
  };
  for (let i=0; i<styleArray.length; i++) {
    if (NO_PREVIOUS_SEG == status) {
      status = HAS_PREVIOUS_SEG;
      createNewSeg(i);
    } else {
        if (previousStyle != styleArray[i]) {
          // should start a new segment after finishing previous segment
          segments[currentSegmentIndex].end = i - 1;

          // and start a new segment
          createNewSeg(i);
        }
    }
  }

  // finish the last segment
  segments[currentSegmentIndex].end = styleArray.length - 1;

  let methodLongNameWithQueryHighlighted = segments.map(function (segment) {
    let clsNames = '';
    if (segment.style & 1)  clsNames += ' suggestion_classname';
    if (segment.style & 2)  clsNames += ' suggestion_method_name';
    if (segment.style & 4)  clsNames += ' suggestion_method_args_list';
    if (segment.style & 8)  clsNames += ' highlight';

    return (<span className={clsNames}>{methodFullName.substring(segment.start, segment.end + 1)}</span>);
  });

  return (
    <span>
      {methodLongNameWithQueryHighlighted}
    </span>
  );
}

class AutoClassLookup extends React.Component { // eslint-disable-line no-undef
  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.onSuggestionsUpdateRequested = this.onSuggestionsUpdateRequested.bind(this);
  }

  onChange(event, { newValue }) {
    this.props.setAutoClassLookupState(Object.assign(this.props.autoClassLookupState, {value: newValue}));
  }

  onSuggestionsUpdateRequested({ value }) {
    this.props.setAutoClassLookupState(Object.assign(this.props.autoClassLookupState,
                                                    {suggestions: getSuggestions(this.props.loadedTargets, value)}));
  }

  render() {
    const { value, suggestions } = this.props.autoClassLookupState;

    const inputProps = {
      placeholder: 'E.g. com.abc.TimeService.getCurrentHour()',
      value: value,
      onChange: this.onChange
    };

    return (
      <span>
        <span style={{paddingRight: '5px'}}>Method:</span>
        <Autosuggest suggestions={suggestions} // eslint-disable-line react/jsx-no-undef
                   onSuggestionsUpdateRequested={this.onSuggestionsUpdateRequested}
                   getSuggestionValue={getSuggestionValue}
                   renderSuggestion={renderSuggestion}
                   inputProps={inputProps}
                   loadedTargets={this.props.loadedTargets}/>
      </span>
    );
  }
}

let modalDefaultStyle = {
  position: 'fixed',
  fontFamily: 'Arial, Helvetica, sans-serif',
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  background: 'rgba(0, 0, 0, 0.8)',
  zIndex: 1000,
  transition: 'opacity 1s ease-in',
  pointerEvents: 'auto',
  overflowY: 'auto'
}

let containerDefaultStyle = {
  width: '680px',
  position: 'relative',
  margin: '6% auto',
  padding: '10px 20px 13px 20px',
  background: '#fff',
  borderRadius: '8px'
}

let closeDefaultStyle = {
  background: '#606061',
  color: '#FFFFFF',
  lineHeight: '25px',
  position: 'absolute',
  right: '-10px',
  textAlign: 'center',
  top: '-8px',
  width: '24px',
  textDecoration: 'none',
  fontWeight: 'bold',
  borderRadius: '12px',
  boxShadow: '1px 1px 3px #000',
  cursor: 'pointer'
}

let MethodRedefine = React.createClass({
  render: function() {
  let returnTypeMessage = ($.isEmptyObject(this.props.autoClassLookupState.returnType))
  ? ''
  : (<span title='return type of this method' style={{marginLeft: '5px'}}>
       <span>{RETURNS_ARROW}</span>
       <span style={{ fontFamily: 'Courier New', fontSize: '16px', marginLeft: '5px'}}>{this.props.autoClassLookupState.returnType}</span>
     </span>);
  return (
    <div>
        <Modal className="test-class" //this will completely overwrite the default css completely
              style={modalDefaultStyle} //overwrites the default background
              containerStyle={containerDefaultStyle} //changes styling on the inner content area
              containerClassName="test"
              closeOnOuterClick={false}
              show={this.props.shown}
              >

          <a style={closeDefaultStyle} onClick={this.props.hideMethodRedefine}>X</a>
          <div>
              <div style={{fontSize: '22px', textAlign: 'center'}}>Redefine a Method</div>
              <div style={{marginLeft: '5px', marginTop : '5px', maxHeight: '420px'}}>
                <div>
                  <AutoClassLookup loadedTargets={this.props.loadedTargets}
                                   setAutoClassLookupState={this.props.setAutoClassLookupState}
                                   autoClassLookupState={this.props.autoClassLookupState} />
                  <br/>
                  {returnTypeMessage}
                </div>
                <div>
                  <textarea rows="8" id="newSource" placeholder="type in source: e.g. { return 10; }" className='code'
                            style={{width: '662px', outline: 'none'}} title='type in source for this method'></textarea>
                </div>
                <div>
                     <span className="tooltip "> An Example
                        <span className="tooltipBelow tooltiptext code " style={{width: '520px', fontSize: '13px', marginLeft: '-90px'}}>
                            <pre><code>{
                             " {\n  java.util.Calendar now = java.util.Calendar.getInstance();\n" +
                             "  return now.get(java.util.Calendar.SECOND); \n" + " }"
                             }</code></pre>
                        </span>
                     </span>
                     <span className="tooltip "> Limitation
                        <span className="tooltipBelow tooltiptext " style={{width: '460px', marginLeft: '-73px'}}>
                          <ul>
                            <li>Use full classname (except java.lang): e.g. java.util.Calendar</li>
                            <li>... see <a href='https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#limit'>Javassist</a> </li>
                          </ul>
                        </span>
                     </span>
                </div>
              </div>
              <div style={{marginTop: '8px', textAlign: 'right', marginRight: '50px'}}>
                <button onClick={this.props.submitMethodRedefine}>Submit</button>
                <button onClick={this.props.hideMethodRedefine}>Close</button>
              </div>
          </div>
        </Modal>
    </div>
  )}
});

let PlayBook = React.createClass({
  render: function(){
    let program = this.props.program;
    let removeMethod = this.props.removeMethod;
    let removeClass = this.props.removeClass;
    let programList = Object.keys(program).map(function (genre) {
      let classList = Object.keys(program[genre]).map(function (clsName) {
        let methodList = Object.keys(program[genre][clsName]).map(function (methodFullName) {
          let methodInfo = extractMethodInfo(methodFullName);
          return (
            <li style={{marginLeft: '-38px', fontSize: '14px'}}>
              <button className='removePlayTarget' onClick={() => removeMethod(genre, methodFullName)} title='Remove the trace or redefinition on this method'><span style={{fontSize:'13px'}}>{CROSS}</span></button>
              <span style={{marginLeft: '4px'}}><span style={{color: 'green'}}>{methodInfo.methodName}</span>(<span style={{fontStyle: 'italic'}}>{methodInfo.methodArgsList}</span>)</span>
            </li>
          )
        });
        return (
           <li style={{marginTop: '2px', marginLeft: '-5px'}}>
             <span>
               <button className='removePlayTarget' onClick={() => removeClass(genre, clsName)} title='Remove the trace or redefinition on this class'><span style={{fontSize:'13px'}}>{CROSS}</span></button>
               <span style={{fontSize: '16px', marginLeft: '4px'}}>{clsName}</span>
             </span>
             <ul style={{marginLeft: '20px', listStyle: 'none'}}>{methodList}</ul>
           </li>
        )
      });
      return (
        <fieldset style={{marginTop: '10px'}}>
          <legend>
            <span style={{fontSize: '18px', margin: '3px'}}>{genre == METHOD_LOGGING ? 'Traced' : 'Redefined'}</span>
          </legend>
           <ul style={{listStyle: 'none', margin: '0px', padding: '0px'}}>
             {classList}
           </ul>
        </fieldset>
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
            <div style={{fontSize: '22px', textAlign: 'center'}}>Manage Methods</div>
            <div style={{overflowX: 'auto', marginTop: '5px', maxHeight: '420px'}}>
              {($.isEmptyObject(program)) ? <p>There are no methods being traced or redefined.</p> : programList}
            </div>
            <div style={{marginTop: '8px', textAlign: 'right', marginRight: '50px'}}>
              <button onClick={this.props.hidePlayBook}>Close</button>
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
          url: '/info/clearTraceLogs',
    });
    this.props.clearLogHistory();
  },
  render: function() {
    return (
        <div style={{display:'inline', paddingLeft: '15px'}}>
          <input name='logFilter' id='logFilter' placeholder='filter logs' onChange={this.props.updateFilter}
                 style={{borderRadius: '4px 0px 0px 4px', borderRight: '0px', outline: 'none', width: '108px'}} />
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
    return {playBookBeingShown: false,
            MethodRedefineIsShow: false};
  },
  showPlayBook: function() {
    this.props.loadProgram();
    this.setState(Object.assign(this.state, {playBookBeingShown: true}));
  },
  hidePlayBook: function() {
    this.setState(Object.assign(this.state, {playBookBeingShown: false}));
  },
  showMethodRedefine: function() {
    this.setState(Object.assign(this.state, {MethodRedefineIsShow: true}));
  },
  hideMethodRedefine: function() {
    this.setState(Object.assign(this.state, {MethodRedefineIsShow: false}));
  },
  validatePlayTarget: function() {
    let methodFullName = this.props.autoClassLookupState.value;  //$("div#content input[type=text]")[0].value.trim();
    if (!methodFullName) {
      this.props.setGlobalMessage(ERROR, 'Please type in a valid classname.methodname!');
      $("div#content input[type=text]")[0].focus();
    }

    return methodFullName;
  },
  submitMethodTrace: function() {
    let methodFullName = this.validatePlayTarget();

    if (methodFullName) {
      this.props.setTraceStarted(true);
      $.ajax({
        url: '/program/addTrace',
        data: { methodFullName: methodFullName },
        success: function(data) {
          this.props.setGlobalMessage(INFO, toMessage(data));
        }.bind(this),
        error: function(data) {
          console.log('error-data', data);
          this.props.setGlobalMessage(ERROR, toErrorMessage(data));
        }.bind(this)
      });
    };
  },
  submitMethodRedefine: function() {
    let longMethodName = this.validatePlayTarget();
    let src = document.getElementById('newSource').value.trim();

    if (!longMethodName || !src) this.props.setGlobalMessage(ERROR, 'A valid classname.methodname and source body must be provided!');

    if (longMethodName && src) {
        $.ajax({
          method: 'post',
          url: '/program/redefine',
          contentType: "application/x-www-form-urlencoded",
          data: { longMethodName: longMethodName,
                  src: src},
          success: function(data) {
            this.props.setGlobalMessage(INFO, toMessage(data));
          }.bind(this),
          error: function(data) {
            this.props.setGlobalMessage(ERROR, toErrorMessage(data));
          }.bind(this)
        });
    }
  },
  render: function() {
    return (
    <div>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} setAutoClassLookupState={this.props.setAutoClassLookupState} autoClassLookupState={this.props.autoClassLookupState}/>
            <button onClick={this.submitMethodTrace} title='trace this method'>Trace</button>
            <button onClick={this.showMethodRedefine} title='Redefine a method using Java code'>Redefine...</button>
            <button onClick={this.showPlayBook} title='show/hide information about method being traced'>Manage...</button>
            <LogControl updateFilter={this.props.updateFilter}
                        clearFilter={this.props.clearFilter}
                        toggleDataSync={this.props.toggleDataSync}
                        clearLogHistory={this.props.clearLogHistory} />
            <MethodRedefine shown={this.state.MethodRedefineIsShow}
                            hideMethodRedefine={this.hideMethodRedefine}
                            loadedTargets={this.props.loadedTargets}
                            autoClassLookupState={this.props.autoClassLookupState}
                            setAutoClassLookupState={this.props.setAutoClassLookupState}
                            submitMethodRedefine={this.submitMethodRedefine} />
            <PlayBook playBookBeingShown={this.state.playBookBeingShown}
                      hidePlayBook={this.hidePlayBook}
                      removeMethod={this.props.removeMethod}
                      removeClass={this.props.removeClass}
                      program={this.props.program}/>
    </div>
    );
  }
});

let LogHistory = React.createClass({
  entryFilter: function(filter, entry) {
    if (!filter || filter == '') {
      return true;
    } else {
      let regex = new RegExp(escapeRegexCharacters(filter.trim()), 'i');
      let methodFullName = entry.classFullName + '.' + entry.methodShortName + '(';
      if (entry.tracePoint == TRIGGER_POINT_ENTER && entry.arguments && entry.arguments.length > 0) {
        methodFullName += entry.arguments.join(',')
      }
      methodFullName += ')';

      return regex.test(methodFullName)
             || (entry.returnedValue && regex.test(entry.returnedValue))
             || (entry.exceptionStackTrace && regex.test(entry.exceptionStackTrace));
    }
  },
  render: function() {
    if (!this.props.traceStarted) {
      return null;
    }

    let hover = this.props.hoverTraceLog;
    let uuidHovered = this.props.traceLogHovered;
    let logList = this.props.logHistory.map(function(entry, idx) {
      if (this.entryFilter(this.props.filter, entry)) {
        let iconClass = null;
        let methodArgs = null;
        let arrow = null;
        let message = null;
        let hasElapsedTime = false;
        let dots = '';
        for (let i=0; i<entry.argsLen; i++) dots += '.';
        switch (entry.tracePoint) {
          case TRIGGER_POINT_ENTER:
            iconClass = 'fa fa-sign-in';
            methodArgs = <span title={entry.type} className='traceLogArgsList' title='arguments'>({entry.arguments ? entry.arguments.join(', ') : ''})</span>
            break;
          case TRIGGER_POINT_RETURNS:
            iconClass = 'fa fa-reply';
            hasElapsedTime = true && entry.elapsed >= 0;
            methodArgs = <span>({dots})</span>;
            arrow = <span> {RETURNS_ARROW} </span>;
            message = <span title={entry.type} className='traceLogReturnedValue' title='return value'>{entry.returnedValue}</span>
            break;
          case TRIGGER_POINT_THROWS_EXCEPTION:
            iconClass = 'fa fa-exclamation-triangle';
            hasElapsedTime = true && entry.elapsed >= 0;
            methodArgs = <span>({dots})</span>;
            arrow = <span> {THROWS_ARROW} </span>;
            message = <span title={entry.type} className='traceLogExceptionStackTrace' title='exception stack trace'>{entry.exceptionStackTrace}</span>
            break;
        }

        let icon = <span className='traceLogIcon'><i className={iconClass}></i></span>;
        let elapsedTimeMessage = hasElapsedTime
                                    ? <span title='elapsed time' style={{textAlign: 'right', whiteSpace: 'nowrap'}}>{formatNumber(entry.elapsed)} ms</span>
                                    : <span></span>;

        let clsNames = 'traceLogRecord';
        clsNames += (idx % 2 == 0) ? ' traceLogEven' : ' traceLogOdd'
        if (uuidHovered == entry.uuid) clsNames += ' sameUuidHighlight';
        return (
          <tr className={clsNames} title={entry.tracePoint} onMouseOver={() => hover(entry.uuid)} onMouseOut={() => hover('no_such_id')} >
            <td style={{width: '1%'}}>{icon}</td>
            <td style={{width: '1%'}}><span title={entry.tracePoint} className='traceLogWhen' style={{whiteSpace: 'nowrap'}} title='when this happened'>{entry.when}</span></td>
            <td className='traceLogClassFullName' title='class name' style={{width: '180px', paddingLeft: '3px'}}><span>{entry.classFullName}.</span></td>
            <td>
              <table style={{borderSpacing: '0px'}}>
                <tr>
                  <td style={{whiteSpace: 'nowrap', paddingTop: '0px'}}>
                    <span className='traceLogMethodShortName' title='method name'>{entry.methodShortName}</span>
                    {methodArgs}
                    {arrow}
                  </td>
                  <td style={{paddingTop: '0px'}}>{message}</td>
                </tr>
              </table>
            </td>
            <td className='traceLogElapsedTime'>{elapsedTimeMessage}</td>
          </tr>
        )
      } else {
        return null;
      };
    }.bind(this));
    return (
      <table className='logHistoryContainer'>
        {logList}
      </table>
    );
  }
});

let JackPlayTitle = React.createClass({
  render: function() {
    return (
      <div style={{paddingBottom: '6px'}}>
        <span>
          <img src="/img/guitar.png" style={{verticalAlign: 'bottom'}}/>
        </span>
        <span style={{fontSize:'32px', fontWeight:'bold'}}>Jackplay!</span>
        <span style={{marginLeft: '15px', color:'#333', fontSize: '16px', fontStyle: 'italic'}} className="fadein">
          "All work and no play makes Jack a dull boy. &mdash; So, let's have Jack play!"
        </span>
      </div>
    )
  }
});

let JackPlay = React.createClass({
  alertOptions: {
        offset: 14,
        position: 'bottom left',
        theme: 'dark',
        time: 5000,
        transition: 'scale'
  },
  getInitialState: function() {
    return {logHistory: [],
            program: [],
            filter: '',
            autoClassLookupState: { value: '', suggestions: [], returnType: ''},
            loadedTargets: [],
            traceStarted: false,
            globalMessage: null,
            traceLogHovered: '',
            isSyncWithServerPaused: false};
  },
  componentDidMount: function() {
    this.syncDataWithServer();
    setInterval(this.checkDataSync, 4200);
  },
  syncDataWithServer: function() {
    $.ajax({
      url: '/info/traceLogs',
      success: function(history) {
        this.setState(Object.assign(this.state, {logHistory: history.map(function(e) {
                                                   if (e.returnedValue == null && e.tracePoint == TRIGGER_POINT_RETURNS) {
                                                     e.returnedValue = 'void';
                                                   }
                                                   return e;
                                                 }),
                                                 traceStarted: history.length > 0 || this.state.traceStarted }));
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
    $.ajax({
      url: '/info/loadedMethods',
      success: function(targets) {
        this.setState(Object.assign(this.state, {loadedTargets: targets}));
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
  },
  loadProgram: function() {
    $.ajax({
      url: '/program/currentProgram',
      success: function(program) {
        this.setState(Object.assign(this.state, {program: program}));
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
    this.clearGlobalMessage();
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
    switch(level) {
      case INFO: this.msg.show(msg, {
                                    time: 5000,
                                    type: 'info',
                                    icon: <span className="fa fa-info-circle" aria-hidden="true"></span>
                                  });
                 break;
      case ERROR: this.msg.show(msg, {
                                    time: 10000,
                                    type: 'error',
                                    icon: <span className="fa fa-question" aria-hidden="true"></span>
                                     });
                  break;
    }

  },
  clearGlobalMessage: function() {
    this.setState(Object.assign(this.state, {globalMessage: null}));
    this.msg.removeAll();
  },
  findReturnTypeOfCurrentLookup: function() {
    let loadedTargets = this.state.loadedTargets;
    let currentLookup = this.state.autoClassLookupState.value;
    for (let target of loadedTargets) {
      if (target.methodFullName == currentLookup) return target.returnType;
    }

    return '';
  },
  setAutoClassLookupState: function(newState) {
    let returnTypeOfCurrentLookup = this.findReturnTypeOfCurrentLookup(newState.value)
    this.setState(Object.assign(this.state, {autoClassLookupState: Object.assign(newState, {returnType: returnTypeOfCurrentLookup})}));

  },
  removeMethod: function(genre, methodFullName) {
    $.ajax({
      url: '/program/undoMethod',
        data: 'methodFullName=' + encodeURIComponent(methodFullName) + '&genre=' + encodeURIComponent(genre),
      success: function(data) {
        this.loadProgram();
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
  },
  removeClass: function(genre, className) {
    $.ajax({
      url: '/program/undoClass',
        data: 'classFullName=' + encodeURIComponent(className) + '&genre=' + encodeURIComponent(genre),
      success: function(data) {
        this.loadProgram();
      }.bind(this),
      error: function(res) {
        console.log("ERROR", res);
      }
    });
  },
  hoverTraceLog: function(uuid) {
    this.setState(Object.assign(this.state, {traceLogHovered: uuid}));
  },
  render: function() {
    return (
    <div>
      <div className='jackPlayHeader'>
        <JackPlayTitle/>
        <PlayPanel loadedTargets={this.state.loadedTargets}
                 setTraceStarted={this.setTraceStarted}
                 updateFilter={this.updateFilter}
                 clearFilter={this.clearFilter}
                 program={this.state.program}
                 loadProgram={this.loadProgram}
                 removeMethod={this.removeMethod}
                 removeClass={this.removeClass}
                 toggleDataSync={this.toggleDataSync}
                 setGlobalMessage={this.setGlobalMessage}
                 autoClassLookupState={this.state.autoClassLookupState}
                 setAutoClassLookupState={this.setAutoClassLookupState}
                 clearLogHistory={this.clearLogHistory} />
      </div>
      <br/>
      <div className='jackPlayTraceLog'>
        <LogHistory logHistory={this.state.logHistory}
                  traceStarted={this.state.traceStarted}
                  filter={this.state.filter}
                  hoverTraceLog={this.hoverTraceLog}
                  traceLogHovered={this.state.traceLogHovered}/>
      </div>
      <AlertContainer ref={itself => this.msg = itself} {...this.alertOptions} />
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);
