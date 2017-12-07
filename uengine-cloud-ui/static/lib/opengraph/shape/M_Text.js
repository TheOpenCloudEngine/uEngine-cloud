/**
 * BPMN : Text Shape
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
OG.shape.bpmn.M_Text = function (label) {
	OG.shape.bpmn.M_Text.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.M_Text';
	this.label = label || 'Text';
	//this.SELECTABLE = false;
	//this.CONNECTABLE = false;
	//this.MOVABLE = false;
};
OG.shape.bpmn.M_Text.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.M_Text.superclass = OG.shape.GeomShape;
OG.shape.bpmn.M_Text.prototype.constructor = OG.shape.bpmn.M_Text;
OG.M_Text = OG.shape.bpmn.M_Text;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.M_Text.prototype.createShape = function () {
	if (this.geom) {
		return this.geom;
	}

	this.geom = new OG.geometry.Rectangle([0, 0], 100, 100);
	this.geom.style = new OG.geometry.Style({
		stroke: "none"
	});

	return this.geom;
};