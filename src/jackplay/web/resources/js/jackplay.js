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
      <div>
        <label htmlFor="playGround">Target to trace: </label>
        <input id='playGround' name='playGround' id='playGround' size="60" placeholder="E.g. com.example.RegistrationService" title='Format: className.methodName'/>
        <button onClick={this.submitMethodLogging}>Play</button>
        <button onClick={this.requestToClearLogHistory}>Clear</button>
        <label className="switch" title='Auto Refresh'>
          <input type="checkbox" defaultChecked='true' onChange={this.props.toggleAutoRefresh}/>
          <div className="slider round"></div>
        </label>
      </div>
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
    return {data: {logHistory: []},
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
        this.setState({data: {logHistory: history}});
      }.bind(this)
    });
  },
  refreshLogHistory: function() {
    if (this.state.autoRefresh) this.loadLogHistoryFromServer();
  },
  clearLogHistory: function() {
    this.setState({data: {logHistory: []}});
  },
  toggleAutoRefresh: function() {
    this.setState({data: this.state.data,
                   autoRefresh: !this.state.autoRefresh})
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.loadLogHistoryFromServer} clearLogHistory={this.clearLogHistory} toggleAutoRefresh={this.toggleAutoRefresh}/>
      <LogHistory logHistory={this.state.data.logHistory} historyLoader={this.loadLogHistoryFromServer}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);