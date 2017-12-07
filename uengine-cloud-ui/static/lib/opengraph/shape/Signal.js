/**
 * BPMN : Signal Activity Shape
 *
 * @class
 * @extends OG.shape.GeomShape
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.Signal = function (label) {
    OG.shape.bpmn.Signal.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.Signal';
    this.label = label;
};
OG.shape.bpmn.Signal.prototype = new OG.shape.bpmn.Event();
OG.shape.bpmn.Signal.superclass = OG.shape.bpmn.Event;
OG.shape.bpmn.Signal.prototype.constructor = OG.shape.bpmn.Signal;
OG.Signal = OG.shape.bpmn.Signal;


/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.Signal.prototype.createShape = function () {
    var geom1, geom2, geom3, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Circle([50, 50], 50);
    geom2 = new OG.geometry.Circle([50, 50], 40);
    geom3 = new OG.Polygon([
        [20, 75],
        [50, 10],
        [80, 75]
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
