
package com.websoft.vantium.mobilescanner.manage;

import android.graphics.Point;

	/*
	0st-------3nd
	 |         |
	 |         |
	 |         |
	1rd-------2th
	 */
	public class CropPoints
	{
		public Point croped[] = new Point[4];
		
		public int inflateWidth;
		public int inflateHeight;
	
		public CropPoints(){
			
			inflateWidth = 0;
			inflateHeight = 0;
			
			for(int i=0; i<4; i++){
				croped[i] = new Point(0,0);
			}
		}
		
		public boolean sortRect(Point[] point){
			
			if (point.length < 4)
				return false;
			
			// get x-pos sort
			for(int i=0; i<4; i++){
				for(int j=i+1; j<4; j++){
					
					if (point[j].x < point[i].x){
						swap(point[j], point[i]);
					}
				}
			}
			
			if (point[1].y > point[0].y){
				croped[0].set(point[0].x, point[0].y);
				croped[1].set(point[1].x, point[1].y);
			}else{
				croped[1].set(point[0].x, point[0].y);
				croped[0].set(point[1].x, point[1].y);
			}
			
			if (point[2].y > point[3].y){
				croped[2].set(point[2].x, point[2].y);
				croped[3].set(point[3].x, point[3].y);
			}else{
				croped[3].set(point[2].x, point[2].y);
				croped[2].set(point[3].x, point[3].y);
			}
			
			int cropW1 = dist(croped[0], croped[3]);
			int cropW2 = dist(croped[1], croped[2]);
			
			int cropH1 = dist(croped[0], croped[1]);
			int cropH2 = dist(croped[2], croped[3]);
			
			inflateWidth = (cropW1 > cropW2) ? cropW1 : cropW2;
			inflateHeight = (cropH1 > cropH2) ? cropH1 : cropH2;
			
			return true;
		}
		
		private int dist(Point p1, Point p2){
			
			int x1 = p1.x;
			int y1 = p1.y;
			int x2 = p2.x;
			int y2 = p2.y;
			
			return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) );
		}
		
		private void swap(Point p1, Point p2){
			
			int x = p1.x; int y = p1.y;
			p1.x = p2.x; p1.y = p2.y;
			p2.x = x; p2.y = y;
		}
	}