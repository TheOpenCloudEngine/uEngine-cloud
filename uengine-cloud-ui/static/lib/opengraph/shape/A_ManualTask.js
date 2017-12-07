/**
 * BPMN : Manual Task Shape
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
OG.shape.bpmn.A_ManualTask = function (label) {
    OG.shape.bpmn.A_ManualTask.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.A_ManualTask';
    this.label = label;
    this.CONNECTABLE = true;
    this.GROUP_COLLAPSIBLE = false;
    this.LoopType = "None";
    this.TaskType = "Manual";
    this.status = "None";
}
OG.shape.bpmn.A_ManualTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_ManualTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_ManualTask.prototype.constructor = OG.shape.bpmn.A_ManualTask;
OG.A_ManualTask = OG.shape.bpmn.A_ManualTask;