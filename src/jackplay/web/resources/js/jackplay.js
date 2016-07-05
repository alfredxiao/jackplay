var Tracing = React.createClass({
  submitMethodLogging: function() {
    $.ajax({
      url: '/jackplay/play',
      data: 'className=' + this.props.className + '&methodName=' + this.props.methodName,
      cache: false,
      success: function(data) {
        console.log("success:", data);
        this.props.setWhatJacksays([data]);
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(err);
      }.bind(this)
    });
  },
  handleClassNameChange: function(e) {
    this.props.className = e.target.value;
  },
  handleMethodNameChange: function(e) {
    this.props.methodName = e.target.value;
  },
  render: function() {
    return (
      <div>
        ClassName: <input name='className' onChange={this.handleClassNameChange}/>,
        methodName: <input name='methodName' onChange={this.handleMethodNameChange}/>
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