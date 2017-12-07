/**
 * BPMN : Loop Task Shape
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
OG.shape.bpmn.A_LoopTask = function (label) {
    OG.shape.bpmn.A_LoopTask.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.A_LoopTask';
    this.label = label;
    this.CONNECTABLE = true;
    this.GROUP_COLLAPSIBLE = false;
    this.LoopType = "Standard";
    this.TaskType = "None";
    this.status = "None";
}
OG.shape.bpmn.A_LoopTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_LoopTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_LoopTask.prototype.constructor = OG.shape.bpmn.A_LoopTask;
OG.A_LoopTask = OG.shape.bpmn.A_LoopTask;