OG.shape.bpmn.A_ScriptTask = function (label) {
  OG.shape.bpmn.A_ScriptTask.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.A_ScriptTask';
  this.label = label;
  this.CONNECTABLE = true;
  this.GROUP_COLLAPSIBLE = false;
  this.LoopType = "None";
  this.TaskType = "User";
  this.status = "None";
}
OG.shape.bpmn.A_ScriptTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_ScriptTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_ScriptTask.prototype.constructor = OG.shape.bpmn.A_ScriptTask;
OG.A_ScriptTask = OG.shape.bpmn.A_ScriptTask;
