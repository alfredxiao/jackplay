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

  return allTargets.filter(entry => regex.test(entry.targetName.substring(0, entry.targetName.indexOf('(')))
                                    || regex.test(entry.targetName));
}

function getSuggestionValue(suggestion) {
  return suggestion.targetName;
}

function useShortTypeName(type) {
  let standardPackage = 'java.lang.'
  if (type.startsWith(standardPackage)) {
    return type.substring(standardPackage.length);
  } else {
    return type;
  }
}

function renderSuggestion(suggestion) {
  var startParen = suggestion.targetName.indexOf('(');
  var classAndMethod = suggestion.targetName.substring(0, startParen);
  var lastDot = classAndMethod.lastIndexOf('.');
  var className = classAndMethod.substring(0, lastDot);
  var methodName = classAndMethod.substring(lastDot + 1, startParen);
  var methodArgsList = suggestion.targetName.substring(startParen + 1, suggestion.targetName.length - 1);
  if (methodArgsList) {
    methodArgsList = methodArgsList.split(',').map(argType => useShortTypeName(argType)).join(', ');
  }
  return (
    <span>
      <span className='suggestion_classname'>{className}.</span>
      <span className='suggestion_method_name'>{methodName}</span>
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
      placeholder: 'E.g. com.example.RegistrationService.createUser',
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


var PlayPanel = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'playGround=' + $("div#content input[type=text]")[0].value
    });
  },
  requestToClearLogHistory: function() {
    $.ajax({
          url: '/clearLogHistory',
    });
    this.props.clearLogHistory();
  },
  render: function() {
    return (
      <table>
        <tr>
          <td>
            <AutoClassLookup loadedTargets={this.props.loadedTargets} />
          </td>
          <td><button onClick={this.submitMethodLogging}>Play</button></td>
          <td><button onClick={this.requestToClearLogHistory}>Clear</button></td>
          <td>
            <div className='checkboxSwitch' title='Switch data sync'>
              <input id='autoSync' type="checkbox" defaultChecked='true' onChange={this.props.toggleDataSync}/>
              <label htmlFor='autoSync'></label>
            </div>
          </td>
        </tr>
      </table>
    );
  }
});

var LogHistory = React.createClass({
  render: function() {
    var logList = this.props.logHistory.map(function(entry) {
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

var JackPlay = React.createClass({
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