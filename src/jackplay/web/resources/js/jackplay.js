var Tracing = React.createClass({
  handler1: function() {
    $.ajax({
      type: 'post',
      url: '/jackplay/play',
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
  render: function() {
    return (
      <div>
        Tracing...
        <button onClick={this.handler1}>Play</button>
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