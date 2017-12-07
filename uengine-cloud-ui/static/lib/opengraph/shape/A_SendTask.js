OG.shape.bpmn.A_SendTask = function (label) {
  OG.shape.bpmn.A_SendTask.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.A_SendTask';
  this.label = label;
  this.CONNECTABLE = true;
  this.GROUP_COLLAPSIBLE = false;
  this.LoopType = "None";
  this.TaskType = "User";
  this.status = "None";
};
OG.shape.bpmn.A_SendTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_SendTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_SendTask.prototype.constructor = OG.shape.bpmn.A_SendTask;
OG.A_SendTask = OG.shape.bpmn.A_SendTask;
