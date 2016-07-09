var PlayPanel = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'className=' + document.getElementById('className').value
          + '&methodName=' + document.getElementById('methodName').value,
      cache: false,
      success: function(data) {
        console.log("success:", data);
        this.props.historyLoader();
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(err);
      }.bind(this)
    });
  },
  render: function() {
    return (
      <div>
        ClassName: <input name='className' id='className'/>,
        methodName: <input name='methodName' id='methodName'/>
        <button onClick={this.submitMethodLogging}>Play</button>
      </div>
    );
  }
});

var LogHistory = React.createClass({
  render: function() {
    var logList = this.props.logHistory.map(function(entry) {
      return (
       <div>
         <span>{entry.when}</span> <span>{entry.log}</span>
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
    setInterval(this.loadLogHistoryFromServer, this.props.pollInterval);
  },
  loadLogHistoryFromServer: function() {
    $.ajax({
      url: '/logHistory',
      cache: false,
      success: function(history) {
        this.setState({data: {logHistory: history}});
      }.bind(this)
    });
  },
  render: function() {
    return (
    <div>
      <PlayPanel historyLoader={this.loadLogHistoryFromServer} />
      <LogHistory logHistory={this.state.data.logHistory} pollInterval="618" historyLoader={this.loadLogHistoryFromServer}/>
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);