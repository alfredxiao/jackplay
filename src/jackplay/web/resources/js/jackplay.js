//<!--input id='playGround' name='playGround' id='playGround' size="60" placeholder="E.g. com.example.RegistrationService" title='Format: className.methodName'/-->
var PlayPanel = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'playGround=' + document.getElementById('playGround').value,
      success: function(data) {
        this.props.historyLoader();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(err);
      }.bind(this)
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
            <label htmlFor="playGround">Target to trace: </label>
            <Select name="playGround" autofocus='true' options={this.props.loadedTargets} placeholder="E.g. com.example.RegistrationService" />
          </td>
          <td><button onClick={this.submitMethodLogging}>Play</button></td>
          <td><button onClick={this.requestToClearLogHistory}>Clear</button></td>
          <td><label className="switch" title='Auto Refresh'>
              <input type="checkbox" defaultChecked='true' onChange={this.props.toggleAutoRefresh}/>
              <div className="slider round"></div>
            </label></td>
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
            loadedTargets: [ {value: 'one', label: 'One' }],
            autoRefresh: true};
  },
  componentDidMount: function() {
    this.loadLogHistoryFromServer();
    setInterval(this.refreshLogHistory, 1218);
  },
  loadLogHistoryFromServer: function() {
    $.ajax({
      url: '/logHistory',
      success: function(history) {
        this.setState(Object.assign(this.state, {logHistory: history}));
      }.bind(this)
    });
  },
  refreshLogHistory: function() {
    if (this.state.autoRefresh) this.loadLogHistoryFromServer();
  },
  clearLogHistory: function() {
    this.setState(Object.assign(this.state, {logHistory: []}));
  },
  toggleAutoRefresh: function() {
    this.setState(Object.assign(this.state, {autoRefresh: !this.state.autoRefresh}));
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.loadLogHistoryFromServer}
                 clearLogHistory={this.clearLogHistory}
                 toggleAutoRefresh={this.toggleAutoRefresh}
                 loadedTargets={this.state.loadedTargets}/>
      <LogHistory logHistory={this.state.logHistory}
                  historyLoader={this.loadLogHistoryFromServer}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);