/**
 * BPMN : Service(Invokation) Task Shape
 *
 * @class
 * @extends OG.shape.bpmn.A_Task
 * @requires OG.common.*
 * @requires OG.geometry.*
 * @requires OG.shape.bpmn.A_Task
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.A_ServiceTask = function (label) {
    OG.shape.bpmn.A_HumanTask.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.A_ServiceTask';
    this.label = label;
    this.CONNECTABLE = true;
    this.GROUP_COLLAPSIBLE = false;
    this.LoopType = "None";
    this.TaskType = "Service";
}
OG.shape.bpmn.A_ServiceTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_ServiceTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_ServiceTask.prototype.constructor = OG.shape.bpmn.A_ServiceTask;
OG.A_ServiceTask = OG.shape.bpmn.A_ServiceTask;