OG.shape.bpmn.ScopeActivity = function (label) {
    OG.shape.bpmn.ScopeActivity.superclass.call(this);

    this.GROUP_COLLAPSIBLE = false;
    this.CONNECTABLE = true;
    this.Events = [];

    this.SHAPE_ID = 'OG.shape.bpmn.ScopeActivity';
    this.label = label;
};

OG.shape.bpmn.ScopeActivity.prototype = new OG.shape.bpmn.M_Group();
OG.shape.bpmn.ScopeActivity.superclass = OG.shape.bpmn.M_Group;
OG.shape.bpmn.ScopeActivity.prototype.constructor = OG.shape.bpmn.ScopeActivity;
OG.ScopeActivity = OG.shape.bpmn.ScopeActivity;


OG.shape.bpmn.ScopeActivity.prototype.layoutChild = function () {
    for(var event in this.Events){
        //TODO:
        //var shapeOfEvent = event.shape;
        //shapeOfEvent.x = .... ;
    }
}