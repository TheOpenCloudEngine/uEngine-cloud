OG.shape.bpmn.A_ReceiveTask = function (label) {
  OG.shape.bpmn.A_ReceiveTask.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.A_ReceiveTask';
  this.label = label;
  this.CONNECTABLE = true;
  this.GROUP_COLLAPSIBLE = false;
  this.LoopType = "None";
  this.TaskType = "User";
  this.status = "None";
}
OG.shape.bpmn.A_ReceiveTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_ReceiveTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_ReceiveTask.prototype.constructor = OG.shape.bpmn.A_ReceiveTask;
OG.A_ReceiveTask = OG.shape.bpmn.A_ReceiveTask;
