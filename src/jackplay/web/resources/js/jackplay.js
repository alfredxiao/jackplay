//<!--input id='playGround' name='playGround' id='playGround' size="60" placeholder="E.g. com.example.RegistrationService" title='Format: className.methodName'/-->
//import {App} from 'auto-class-lookup';

// https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
function escapeRegexCharacters(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getSuggestions(allTargets, value) {
  const escapedValue = escapeRegexCharacters(value.trim());

  if (escapedValue === '') {
    return [];
  }

  const regex = new RegExp(escapedValue, 'i');

  return allTargets.filter(entry => regex.test(entry.targetName));
}

function getSuggestionValue(suggestion) {
  return suggestion.targetName;
}

function renderSuggestion(suggestion) {
  return (
    <span>{suggestion.targetName}</span>
  );
}

class AutoClassLookup extends React.Component { // eslint-disable-line no-undef
  constructor() {
    super();
    console.log(this);

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

  onSuggestionSelected(e, {suggestionValue}) {
    console.log("suggestedValue:" + suggestionValue);
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
                   onSuggestionSelected={this.onSuggestionSelected}
                   inputProps={inputProps}
                   loadedTargets66={this.props.loadedTargets}/>
    );
  }
}


var PlayPanel = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'playGround=' + $("div#content input[type=text]")[0].value //this.props.getSelectedTarget(),
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
            <AutoClassLookup loadedTargets={this.props.loadedTargets} setSelectedTarget={this.props.setSelectedTarget}/>
          </td>
          <td><button onClick={this.submitMethodLogging}>Play</button></td>
          <td><button onClick={this.requestToClearLogHistory}>Clear</button></td>
          <td><label>Sync logs from server
                <input type="checkbox" defaultChecked='true' onChange={this.props.toggleDataSync}/>
              </label>
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
      <div>
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
    setInterval(this.checkDataSync, 1218);
  },
  syncDataWithServer: function() {
    $.ajax({
      url: '/logHistory',
      success: function(history) {
        this.setState(Object.assign(this.state, {logHistory: history}));
      }.bind(this)
    });
    $.ajax({
      url: '/loadedTargets',
      success: function(targets) {
        this.setState(Object.assign(this.state, {loadedTargets: targets}));
      }.bind(this)
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
  setSelectedTarget: function(targetName) {
    this.setState(Object.assign(this.state, {selectedTarget: targetName}))
  },
  getSelectedTarget: function() {
    return this.state.selectedTarget;
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.syncDataWithServer}
                 clearLogHistory={this.clearLogHistory}
                 toggleDataSync={this.toggleDataSync}
                 loadedTargets={this.state.loadedTargets}
                 setSelectedTarget={this.setSelectedTarget}
                 getSelectedTarget={this.getSelectedTarget}/>
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