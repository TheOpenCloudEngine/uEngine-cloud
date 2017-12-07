/**
 * BPMN : Compensation End Event Shape
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
OG.shape.bpmn.E_End_Compensation = function (label) {
    OG.shape.bpmn.E_End_Compensation.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.E_End_Compensation';
    this.label = label;
};
OG.shape.bpmn.E_End_Compensation.prototype = new OG.shape.bpmn.E_End();
OG.shape.bpmn.E_End_Compensation.superclass = OG.shape.bpmn.E_End;
OG.shape.bpmn.E_End_Compensation.prototype.constructor = OG.shape.bpmn.E_End_Compensation;
OG.E_End_Compensation = OG.shape.bpmn.E_End_Compensation;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_End_Compensation.prototype.createShape = function () {
    var geom1, geom2, geom3, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Circle([50, 50], 50);
    geom1.style = new OG.geometry.Style({
        "stroke-width": 3
    });

    geom2 = new OG.geometry.Polygon([
        [15, 50],
        [45, 70],
        [45, 30]
    ]);
    geom2.style = new OG.geometry.Style({
        "fill": "black",
        "fill-opacity": 1
    });

    geom3 = new OG.geometry.Polygon([
        [45, 50],
        [75, 70],
        [75, 30]
    ]);
    geom3.style = new OG.geometry.Style({
        "fill": "black",
        "fill-opacity": 1
    });

    geomCollection.push(geom1);
    geomCollection.push(geom2);
    geomCollection.push(geom3);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom'
    });

    return this.geom;
};
