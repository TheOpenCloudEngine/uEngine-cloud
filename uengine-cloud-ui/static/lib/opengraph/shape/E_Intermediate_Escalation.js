OG.shape.bpmn.E_Intermediate_Escalation = function (label) {
    OG.shape.bpmn.E_Intermediate_Escalation.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Escalation';
    this.label = label;
};
OG.shape.bpmn.E_Intermediate_Escalation.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Escalation.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Escalation.prototype.constructor = OG.shape.bpmn.E_Intermediate_Escalation;
OG.E_Intermediate_Escalation = OG.shape.bpmn.E_Intermediate_Escalation;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Escalation.prototype.createShape = function () {
    var geom1, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Polygon([
        [20, 80],
        [50, 20],
        [80, 80],
        [50, 50]
    ]);

    geomCollection.push(new OG.geometry.Circle([50, 50], 50));
    geomCollection.push(new OG.geometry.Circle([50, 50], 44));
    geomCollection.push(geom1);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom',
        // "stroke" : "#969149",
        "stroke-width" : 1.5,
        fill : "white",
        "fill-opacity" : 1
    });

    return this.geom;
};
