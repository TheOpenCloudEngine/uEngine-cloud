/**
 * BPMN : WebService(Invokation) Task Shape
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
OG.shape.bpmn.A_WebServiceTask = function (label) {
    OG.shape.bpmn.A_WebServiceTask.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.A_WebServiceTask';
    this.label = label;
    this.CONNECTABLE = true;
    this.GROUP_COLLAPSIBLE = false;
    this.LoopType = "None";
    this.TaskType = "WebService";
}
OG.shape.bpmn.A_WebServiceTask.prototype = new OG.shape.bpmn.A_Task();
OG.shape.bpmn.A_WebServiceTask.superclass = OG.shape.bpmn.A_Task;
OG.shape.bpmn.A_WebServiceTask.prototype.constructor = OG.shape.bpmn.A_WebServiceTask;
OG.A_WebServiceTask = OG.shape.bpmn.A_WebServiceTask;