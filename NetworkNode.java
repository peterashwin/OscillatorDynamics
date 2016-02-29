import java.util.Vector;

class NetworkNode {
	public int 			id;
	public String 			stateString;
	public int 			hitCount;
	public Vector<NetworkNode>	childNodes;

	public NetworkNode( int id, String stateString ) {
		childNodes = new Vector<NetworkNode>();
		this.id = id;
		this.stateString = stateString;
		this.hitCount = 0;
	}
}
