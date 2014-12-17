package process;

import java.io.IOException;
import java.util.ArrayList;

import math.geom2d.line.LineSegment2D;
import math.geom3d.Box3D;
import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.Surface3D;

/**
 * Stores info on a slice job and the layers which make it up.
 */
public class Slicer {
	public final Model3D part;
	public final Surface3D shape;
	public final double layerHeight;
	public final double filD;
	public final double nozzleD;
	public final double extrusionWidth;
	public final int printTemp;
	public final double Speed;
	public final int numShells;
	public final double infillWidth;
	public final double infillDir;	//Direction of infill on layer 0;
	public final double infillAngle;	//Amount to change infill direction each layer, radians CW.
	public final double lift;	//Amount to lift for travel moves.
	public final LineSegment2D[] topo;
	public final double EperL;	//E increase per unit L increase.
	//Inputs below are optional, above are mandatory.
	public double shellSpeedMult = 1;
	public double bottomSpeedMult = 1;
	public int bottomLayerCount = 0;
	public double infillInsetMultiple = 0;	//Number of extrusion widths to inset infill beyond innermost shell
	public Slicer(Model3D part, Surface3D shape, double layerHeight, double filD, double nozzleD, double extrusionWidth,
			int printTemp, double speed, int numShells, double infillWidth, double infillDir, double infillAngle, 
			double lift) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.extrusionWidth = extrusionWidth;
		this.printTemp = printTemp;
		this.Speed = speed;
		this.numShells = numShells;
		this.infillWidth = infillWidth;
		this.layerHeight = layerHeight;
		this.infillDir = infillDir;
		this.infillAngle = infillAngle;
		this.part = part;
		this.shape = shape;
		this.topo = shape.topology();
		this.lift = lift;
		this.EperL = ((extrusionWidth-layerHeight)*extrusionWidth+3.14*layerHeight*layerHeight/4);
	}
	/**
	 * Position shape so that its highest point is layerHeight/2 above the part's lowest point.
	 */
	private void PositionShape(){
		Box3D b1 = shape.boundingBox();
		Box3D b2 = part.boundingBox();
		Vector3D mv = new Vector3D(0,0,b2.getMinZ()-b1.getMaxZ()+layerHeight/2);
		shape.move(mv);
	}
	private int layerCount(){
		Box3D b1 = shape.boundingBox();
		Box3D b2 = part.boundingBox();
		//Distance between highest point on part and the lowest point on a properly positioned shape.
		double distance = b1.getHeight()+b2.getHeight()-layerHeight/2;
		return (int) Math.ceil(distance/layerHeight);
	}
	public void slice(String fileLoc){
		PositionShape();
		int lc = layerCount();
		GcodeExport g = new GcodeExport(fileLoc, this);
		g.writeFromFile("start.gcode");
		for(int n=0;n<lc;n++){
			Layer l = new Layer(this,n);
			Reproject r = new Reproject(l.offset,this);
			ArrayList<Extrusion2D> p = l.getPath();
			if(p==null) continue;
			ArrayList<Extrusion3D> path = r.Proj(p);
			g.addLayer(path);			
		}
		g.writeFromFile("end.gcode");
		g.close();
	}
}