/**
 * BPMN : Mapper Task Shape
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
OG.shape.bpmn.A_MapperTask = function (label) {
    OG.shape.bpmn.A_MapperTask.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.A_MapperTask';
    this.label = label;
    this.CONNECTABLE = true;
    this.GROUP_COLLAPSIBLE = false;
    this.LoopType = "None";
    this.TaskType = "Mapper";
    this.status = "None";
}
OG.shape.bpmn.A_MapperTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_MapperTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_MapperTask.prototype.constructor = OG.shape.bpmn.A_MapperTask;
OG.A_MapperTask = OG.shape.bpmn.A_MapperTask;