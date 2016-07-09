var PlayPanel = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'className=' + document.getElementById('className').value
          + '&methodName=' + document.getElementById('methodName').value,
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
        Class Name: <input name='className' id='className' size="45" placeholder="Please input a class name"/>,
        Method Name: <input name='methodName' id='methodName' size="33" placeholder="Please give a method name"/>
        <button onClick={this.submitMethodLogging}>Play</button>
        <button onClick={this.requestToClearLogHistory}>Clear</button>
        <label className="switch">
          <input type="checkbox" />
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
    return {data: {logHistory: []}};
  },
  componentDidMount: function() {
    this.loadLogHistoryFromServer();
    setInterval(this.loadLogHistoryFromServer, 1218);
  },
  loadLogHistoryFromServer: function() {
    $.ajax({
      url: '/logHistory',
      success: function(history) {
        this.setState({data: {logHistory: history}});
      }.bind(this)
    });
  },
  clearLogHistory: function() {
    this.setState({data: {logHistory: []}});
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.loadLogHistoryFromServer} clearLogHistory={this.clearLogHistory}/>
      <LogHistory logHistory={this.state.data.logHistory} historyLoader={this.loadLogHistoryFromServer}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);