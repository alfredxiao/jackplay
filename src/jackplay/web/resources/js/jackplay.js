var Tracing = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/logMethod',
      data: 'className=' + document.getElementById('className').value
          + '&methodName=' + document.getElementById('methodName').value,
      cache: false,
      success: function(data) {
        console.log("success:", data);
        var h1 = this.props.setWhatJacksays;
        $.ajax({
          url: '/logHistory',
          cache: false,
          success: function(history) {
            h1(history);
          }
        });
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

var JackSays = React.createClass({
  render: function() {
    return (
      <div>
        {this.props.jacksays}
      </div>
    );
  }
});

var JackPlay = React.createClass({
  getInitialState: function() {
    return {data: {jacksays: ['initial data']}};
  },
  setWhatJacksays: function(jacksays) {
    console.log("jacksays is set with ", jacksays);
    //this.state.data.jacksays = jacksays;
    this.setState({data: {jacksays: jacksays}});
    console.log(this.state);
  },
  render: function() {
    return (
    <div>
      <Tracing setWhatJacksays={this.setWhatJacksays} />
      <JackSays jacksays={this.state.data.jacksays} />
    </div>
    );
    }
  });

ReactDOM.render(
  <JackPlay />,
  document.getElementById('content')
);