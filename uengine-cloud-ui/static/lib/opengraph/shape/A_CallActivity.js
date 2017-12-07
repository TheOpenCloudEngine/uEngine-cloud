OG.shape.bpmn.A_CallActivity = function (label) {
  OG.shape.bpmn.A_CallActivity.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.A_CallActivity';
  this.label = label;
  this.CONNECTABLE = true;
  this.GROUP_COLLAPSIBLE = false;
  this.LoopType = "None";
  this.TaskType = "User";
  this.status = "None";
}
OG.shape.bpmn.A_CallActivity.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_CallActivity.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_CallActivity.prototype.constructor = OG.shape.bpmn.A_CallActivity;
OG.A_CallActivity = OG.shape.bpmn.A_CallActivity;
