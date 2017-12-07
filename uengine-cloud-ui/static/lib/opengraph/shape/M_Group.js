/**
 * BPMN : Group Shape
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
OG.shape.bpmn.M_Group = function (label) {
    OG.shape.bpmn.M_Group.superclass.call(this);


    this.CONNECTABLE = true;


    this.SHAPE_ID = 'OG.shape.bpmn.M_Group';
    this.label = label;
};


OG.shape.bpmn.M_Group.prototype = new OG.shape.GroupShape();
OG.shape.bpmn.M_Group.superclass = OG.shape.GroupShape;
OG.shape.bpmn.M_Group.prototype.constructor = OG.shape.bpmn.M_Group;
OG.M_Group = OG.shape.bpmn.M_Group;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.M_Group.prototype.createShape = function () {
    if (this.geom) {
        return this.geom;
    }

    this.geom = new OG.geometry.Rectangle([0, 0], 100, 100);
    this.geom.style = new OG.geometry.Style({
        //'stroke-dasharray': '- ',
        'r': 6,
        'fill': '#ffffff',
        'fill-opacity': 0,
        "vertical-align": "top",
        "text-anchor": "start"

    });

    return this.geom;
};