OG.shape.bpmn.A_BusinessTask = function (label) {
  OG.shape.bpmn.A_BusinessTask.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.A_BusinessTask';
  this.label = label;
  this.CONNECTABLE = true;
  this.GROUP_COLLAPSIBLE = false;
  this.LoopType = "None";
  this.TaskType = "User";
  this.status = "None";
}
OG.shape.bpmn.A_BusinessTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_BusinessTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_BusinessTask.prototype.constructor = OG.shape.bpmn.A_BusinessTask;
OG.A_BusinessTask = OG.shape.bpmn.A_BusinessTask;
