
import java.util.ArrayList;
import java.util.Collections;



public class AStarPathFinder{
	//nodes that have been searched
	private ArrayList closed = new ArrayList();
	//nodes that havnt been searched
	private SortedList open = new SortedList();
	private int maxSearchDistance;
	private static int[] currentcoord = new int[2];
	private static int[] targetcoord = new int[2];
	private static int[] enemycoord = new int[2];
	//add height and width, +1 to match coordinates from 0 to x,y
	public int fieldwidth=16;
	public int fieldheight=16;
	//array of all the coordinates ( nodes ) 
	private Node[][] nodes;
	private boolean allowDiagMovement;
	//private AStarHeuristic heuristic;
	//array of all the blocked coords
	private boolean[][] blocked;
	//array of all the visited coords
	private boolean[][] visited;

	public AStarPathFinder(int maxSearchDistance, int fieldwidth, int fieldheight) {
		//this.heuristic = heuristic;
		this.fieldwidth = fieldwidth+1;
		this.fieldheight = fieldheight+1;
		blocked = new boolean[this.fieldwidth][this.fieldheight];
		visited = new boolean[this.fieldwidth][this.fieldheight];
		this.maxSearchDistance = maxSearchDistance;
		this.allowDiagMovement = true;
		nodes = new Node[this.fieldwidth][this.fieldheight];
		for (int x=0;x<this.fieldwidth;x++) {
			for (int y=0;y<this.fieldheight;y++) {
				nodes[x][y] = new Node(x,y);
			}
		}
		
	}

	/**
	 * @see PathFinder#findPath(Mover, int, int, int, int)
	 */
	public Path findPath(int sx, int sy, int tx, int ty) {
		boolean changes = false;
		boolean changet = false;
		if (blocked(sx,sy)){
			removeBlock(sx,sy);
			changes = true;
		}
		if (blocked(tx,ty)){
			removeBlock(tx,ty);
			changet = true;
		}
		/*// easy first check, if the destination is blocked, we can't get there
		if (blocked(tx, ty)) {
			return null;
		}*/
		Path path = new Path();
		try{
		// initial state for A*. The closed group is empty. Only the starting
		// tile is in the open list and it's cost is zero, i.e. we're already there
		nodes[sx][sy].cost = 0;
		nodes[sx][sy].depth = 0;
		closed.clear();
		open.clear();
		open.add(nodes[sx][sy]);
		nodes[tx][ty].parent = null;
		// while we haven't found the goal and haven't exceeded our max search 	epth
		int maxDepth = 0;
		while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
			// pull out the first node in our open list, this is determined to 
			// be the most likely to be the next step based on our heuristic
			Node current = getFirstInOpen();
			if (current == nodes[tx][ty]) {
				break;
			}
			removeFromOpen(current);
			addToClosed(current);
			// search through all the neighbours of the current node evaluating
			// them as next steps
			for (int x=-1;x<2;x++) {
				for (int y=-1;y<2;y++) {
					// not a neighbour, its the current tile
					if ((x == 0) && (y == 0)) {
						continue;
					}
					// if we're not allowing diaganol movement then only 
					// one of x or y can be set
					if (!allowDiagMovement) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					// determine the location of the neighbour and evaluate it
					int xp = x + current.x;
					int yp = y + current.y;
					if (isValidLocation(sx,sy,xp,yp)) {
						// the cost to get to this node is cost the current plus the movement
						// cost to reach this node. Note that the heursitic value is only used
						// in the sorted open list
						float nextStepCost = current.cost + getMovementCost(current.x, current.y, xp, yp);
						Node neighbour = nodes[xp][yp];
						pathFinderVisited(xp, yp);
						// if the new cost we've determined for this node is lower than 
						// it has been previously makes sure the node hasn't been discarded. We've
						// determined that there might have been a better path to get to
						// this node so it needs to be re-evaluated
						if (nextStepCost < neighbour.cost) {
							if (inOpenList(neighbour)) {
								removeFromOpen(neighbour);
							}
							if (inClosedList(neighbour)) {
								removeFromClosed(neighbour);
							}
						}
						// if the node hasn't already been processed and discarded then
						// reset it's cost to our current cost and add it as a next possible
						// step (i.e. to the open list)
						if (!inOpenList(neighbour) && !(inClosedList(neighbour))) {
							neighbour.cost = nextStepCost;
							neighbour.heuristic = getCost(xp, yp, tx, ty);
							maxDepth = Math.max(maxDepth, neighbour.setParent(current));
							addToOpen(neighbour);
						}
					}
				}
			}
		}
		
		// since we've got an empty open list or we've run out of search 
		// there was no path. Just return null
		if (nodes[tx][ty].parent == null) {
			return null;
		}
		// At this point we've definitely found a path so we can uses the parent
		// references of the nodes to find out way from the target location back
		// to the start recording the nodes on the way.
		path = new Path();
		Node target = nodes[tx][ty];
		while (target != nodes[sx][sy]) {
			path.prependStep(target.x, target.y);
			target = target.parent;
		}
		path.prependStep(sx,sy);
		if(changes){
			addBlock(sx,sy);
		}
		if(changet){
			addBlock(tx,ty);
		}
		// thats it, we have our path 
		return path;
		} catch (Exception e){
			System.out.println("Exception in finding path!");
			e.printStackTrace();
			System.out.println(sx);
			System.out.println(sy);
			System.out.println(tx);
			System.out.println(ty);
			path.print();
			System.out.println("Status");
			for (int x=0;x<fieldwidth;x++) {
				for (int y=0;y<fieldheight;y++) {
					System.out.println(x + "," + y + ":" + blocked(x,y));
				}
			}
			return null;
			
		}
	}
	private void pathFinderVisited(int xp, int yp) {
		// TODO add code to check if it has already been visited. Closed list?
		visited[xp][yp] = true;
	}
	//fills blocked list from position of opponent, needs to be called each refresh
	private void buildblocked(int tx, int ty){
		//clears blocked listarray, refills with new position of enemy robot
		for(int x = 0; x<fieldwidth; x++){
			for(int y = 0; y<fieldheight; y++){
				blocked[x][y] = false;
			}
		}
		//add obstacles here by coordinate, possibly exapand too for larger size
		blocked[enemycoord[0]][enemycoord[1]] = true;
	}
	public boolean blocked(int tx, int ty) {
		// TODO add code to find if piece is blocked i.e other robot 
		//or obstacles from vision
			return blocked[tx][ty];

	}
	/**
	 * Get the first element from the open list. This is the next
	 * one to be searched.
	 * 
	 * @return The first element in the open list
	 */
	protected Node getFirstInOpen() {
		return (Node) open.first();
	}
	/**
	 * Add a node to the open list
	 * 
	 * @param node The node to be added to the open list
	 */
	protected void addToOpen(Node node) {
		open.add(node);
	}
	/**
	 * Check if a node is in the open list
	 * 
	 * @param node The node to check for
	 * @return True if the node given is in the open list
	 */
	protected boolean inOpenList(Node node) {
		return open.contains(node);
	}
	/**
	 * Remove a node from the open list
	 * 
	 * @param node The node to remove from the open list
	 */
	protected void removeFromOpen(Node node) {
		open.remove(node);
	}
	/**
	 * Add a node to the closed list
	 * 
	 * @param node The node to add to the closed list
	 */
	protected void addToClosed(Node node) {
		closed.add(node);
	}
	/**
	 * Check if the node supplied is in the closed list
	 * 
	 * @param node The node to search for
	 * @return True if the node specified is in the closed list
	 */
	protected boolean inClosedList(Node node) {
		return closed.contains(node);
	}
	/**
	 * Remove a node from the closed list
	 * 
	 * @param node The node to remove from the closed list
	 */
	protected void removeFromClosed(Node node) {
		closed.remove(node);
	}
	/**
	 * Check if a given location is valid for the supplied mover
	 * 
	 * @param mover The mover that would hold a given location
	 * @param sx The starting x coordinate
	 * @param sy The starting y coordinate
	 * @param x The x coordinate of the location to check
	 * @param y The y coordinate of the location to check
	 * @return True if the location is valid for the given mover
	 */
	protected boolean isValidLocation(int sx, int sy, int x, int y) {
		boolean invalid = (x < 0) || (y < 0) || (x >= fieldwidth) || (y >= fieldheight);
		
		if ((!invalid) && ((sx != x) || (sy != y))) {
			invalid = blocked(x, y);
		}	
		return !invalid;
	}
	/**
	 * Get the cost to move through a given location
	 * 
	 * @param mover The entity that is being moved
	 * @param sx The x coordinate of the tile whose cost is being determined
	 * @param sy The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The cost of movement through the given tile
	 */
	public float getMovementCost(int sx, int sy, int tx, int ty) {
		return 1;
	}

	/**
	 * A simple sorted list
	 *
	 * @author kevin
	 */
	private class SortedList {
		/** The list of elements */
		private ArrayList list = new ArrayList();	
		/**
		 * Retrieve the first element from the list
		 *  
		 * @return The first element from the list
		 */
		public Object first() {
			return list.get(0);
		}
		/**
		 * Empty the list
		 */
		public void clear() {
			list.clear();
		}
		/**
		 * Add an element to the list - causes sorting
		 * 
		 * @param o The element to add
		 */
		public void add(Object o) {
			list.add(o);
			Collections.sort(list);
		}
		/**
		 * Remove an element from the list
		 * 
		 * @param o The element to remove
		 */
		public void remove(Object o) {
			list.remove(o);
		}
		/**
		 * Get the number of elements in the list
		 * 
		 * @return The number of element in the list
 		 */
		public int size() {
			return list.size();
		}
		/**
		 * Check if an element is in the list
		 * 
		 * @param o The element to search for
		 * @return True if the element is in the list
		 */
		public boolean contains(Object o) {
			return list.contains(o);
		}
	}
	/**
	 * A single node in the search graph
	 */
	private class Node implements Comparable {
		/** The x coordinate of the node */
		private int x;
		/** The y coordinate of the node */
		private int y;
		/** The path cost for this node */
		private float cost;
		/** The parent of this node, how we reached it in the search */
		private Node parent;
		/** The heuristic cost of this node */
		private float heuristic;
		/** The search depth of this node */
		private int depth;
		
		/**
		 * Create a new node
		 * 
		 * @param x The x coordinate of the node
		 * @param y The y coordinate of the node
		 */
		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}	
		/**
		 * Set the parent of this node
		 * 
		 * @param parent The parent node which lead us to this node
		 * @return The depth we have no reached in searching
		 */
		public int setParent(Node parent) {
			depth = parent.depth + 1;
			this.parent = parent;	
			return depth;
		}
		/**
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(Object other) {
			Node o = (Node) other;
			float f = heuristic + cost;
			float of = o.heuristic + o.cost;
			if (f < of) {
				return -1;
			} else if (f > of) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	public float getCost(int x, int y, int tx, int ty) {		
		float dx = tx - x;
		float dy = ty - y;
		float result = (float) (Math.sqrt((dx*dx)+(dy*dy)));
		return result;
	}
	public void addBlock(int xfrm, int xto, int yfrm, int yto){
		//using robot size 90 by 80 assuming coordinates are centre
		for(int bx = xfrm; bx<xto; bx++){
			for(int by = yfrm; by<yto; by++){
				blocked[bx][by] = true;
			}		
		}
	}
	
	public void addBlock(int x, int y){
		blocked[x][y] = true;
	}
	
	public void removeBlock(int x, int y){
		blocked[x][y] = false;
	}
/*	public static void main(String[] argv) {
		//test data, 	
		AStarPathFinder finder = new AStarPathFinder(500);
		finder.addBlock(9,1);
		Path testpath = finder.findPath(10, 0, 2, 4);

		testpath.print();
	}*/
}
