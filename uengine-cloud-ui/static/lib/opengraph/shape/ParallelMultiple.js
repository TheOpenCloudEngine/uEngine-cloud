OG.shape.bpmn.ParallelMultiple = function (label) {
    OG.shape.bpmn.ParallelMultiple.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.ParallelMultiple';
    this.label = label;
};
OG.shape.bpmn.ParallelMultiple.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.ParallelMultiple.superclass = OG.shape.GeomShape;
OG.shape.bpmn.ParallelMultiple.prototype.constructor = OG.shape.bpmn.ParallelMultiple;
OG.ParallelMultiple = OG.shape.bpmn.ParallelMultiple;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.ParallelMultiple.prototype.createShape = function () {
    var geom1, geom2, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Circle([50, 50], 50);
    geom2 = new OG.geometry.Circle([50, 50], 40);
    geom3 = new OG.Polygon([
        [20, 40],
        [20, 60],
        [40, 60],
        [40, 80],
        [60, 80],
        [60, 60],
        [80, 60],
        [80, 40],
        [60, 40],
        [60, 20],
        [40, 20],
        [40, 40]
    ]);

    geomCollection.push(geom1);
    geomCollection.push(geom2);
    geomCollection.push(geom3);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom',
        "stroke-width": 1.5,
        "stroke" : "#969149",
        fill: "white",
        "fill-opacity": 1
    });

    return this.geom;
};