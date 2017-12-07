OG.shape.bpmn.MISequential = function (label) {
    OG.shape.bpmn.MISequential.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.MISequential';
    this.label = label;
};
OG.shape.bpmn.MISequential.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.MISequential.superclass = OG.shape.GeomShape;
OG.shape.bpmn.MISequential.prototype.constructor = OG.shape.bpmn.MISequential;
OG.MISequential = OG.shape.bpmn.MISequential;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.MISequential.prototype.createShape = function () {
    var geom1, geom2, geom3, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Line([0, 30], [30, 30]);
    geom2 = new OG.geometry.Line([0, 15], [30, 15]);
    geom3 = new OG.geometry.Line([0, 0], [30, 0]);

    geomCollection.push(geom1);
    geomCollection.push(geom2);
    geomCollection.push(geom3);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    return this.geom;
};