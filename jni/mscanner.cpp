#include "common.h"

#include <queue>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

using namespace std;
using namespace cv;


Mat matEdit;

//----------------------------------------------------------------------//
/*
1st-------4nd
 |         |
 |         |
 |         |
2rd-------3th
 */

Mat magicMat(Mat src, double alpha, int beta){

	Mat dst = Mat::zeros( src.size(), src.type() );
	for( int y = 0; y < src.rows; y++ )
		{ for( int x = 0; x < src.cols; x++ )
			 { for( int c = 0; c < 3; c++ )
				  {
				 dst.at<Vec3b>(y,x)[c] = saturate_cast<uchar>( alpha*( src.at<Vec3b>(y,x)[c] ) + beta );
				 }
		}
	}

	return dst;
}

// angle = 0, 90, 180, 270
Mat rotateMat(Mat src, int angle)
{
	Mat rotated;

	switch(angle){
	case 0:
		rotated = src;
		break;
	case 90:
		cv::transpose(src, rotated);
		cv::flip(rotated, rotated, 1);
		break;
	case 180:
		cv::transpose(src, rotated);
		cv::flip(rotated, rotated, 1);
		cv::transpose(rotated, rotated);
		cv::flip(rotated, rotated, 1);
		break;
	case 270:
		cv::transpose(src, rotated);
		cv::flip(rotated, rotated, 1);
		cv::transpose(rotated, rotated);
		cv::flip(rotated, rotated, 1);
		cv::transpose(rotated, rotated);
		cv::flip(rotated, rotated, 1);
		break;
	}

	return rotated;
}

int dist(int x1, int y1, int x2, int y2)
{
	int x = x1 - x2;
	int y = y1 - y2;

	return sqrt(x*x + y*y);
}

// rotate = 0, 90, 180, 270
int doBatchWork(JNIEnv *env,
				char* srcPath,
				char* dstPath,
				int mode,
				int angle,
				int inflateW, int inflateH,
				int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4)
{

	int w1 = dist(x1, y1, x4, y4);
	int w2 = dist(x2, y2, x3, y3);
	inflateW = (w1 > w2) ? w1 : w2;

	int h1 = dist(x1, y1, x2, y2);
	int h2 = dist(x3, y3, x4, y4);
	inflateH = (h1 > h2) ? h1 : h2;


	LOGD("doBatchWork rotate=%d, inflateW=%d, inflateH=%d, x,y=%d-%d, %d-%d, %d-%d, %d-%d",
			angle, inflateW, inflateH, x1, y1, x2, y2, x3, y3, x4, y4);

	Mat srcImg = imread(srcPath, CV_LOAD_IMAGE_COLOR);   // Read the file

	if(! srcImg.data )                              // Check for invalid input
	{
		LOGE("doBatchWork error");
		return -1;
	}

	//Mat srcImg = src.clone();

	//cv::flip(srcImg, srcImg, 0);
	LOGD("doBatchWork 1, cropImage w=%d, h=%d", srcImg.cols, srcImg.rows);

	srcImg = rotateMat(srcImg, angle);

	LOGD("doBatchWork 2, cropImage w=%d, h=%d", srcImg.cols, srcImg.rows);

	// convert
	cv::Point2f src_vertices[4];
	src_vertices[0] = Point((float)x1, (float)y1);
	src_vertices[1] = Point((float)x2, (float)y2);
	src_vertices[2] = Point((float)x3, (float)y3);
	src_vertices[3] = Point((float)x4, (float)y4);

	cv::Point2f dst_vertices[4];
	dst_vertices[0] = Point(0, 0);
	dst_vertices[1] = Point(0, (float)inflateH-1);
	dst_vertices[2] = Point((float)inflateW-1, (float)inflateH-1);
	dst_vertices[3] = Point((float)inflateW-1, 0);

	Mat warpMatrix = getPerspectiveTransform(src_vertices, dst_vertices);

	LOGD("doBatchWork 2");

	// inflate.
	warpPerspective(srcImg, matEdit, warpMatrix, Size(inflateW, inflateH));

	return setEditMode(mode, dstPath);
}

// rotate = 0, 90, 180, 270
int cropImage(JNIEnv *env,
				char* srcPath,
				char* dstPath,
				int angle,
				int inflateW, int inflateH,
				int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4)
{

	LOGD("cropImage rotate=%d, inflateW=%d, inflateH=%d, x,y=%d-%d, %d-%d, %d-%d, %d-%d",
			angle, inflateW, inflateH, x1, y1, x2, y2, x3, y3, x4, y4);

	Mat srcImg = imread(srcPath, CV_LOAD_IMAGE_COLOR);   // Read the file

	if(! srcImg.data )                              // Check for invalid input
	{
		LOGE("cropImage error");
		return -1;
	}

	//Mat srcImg = src.clone();

	//cv::flip(srcImg, srcImg, 0);
	LOGD("cropImage 1, cropImage w=%d, h=%d", srcImg.cols, srcImg.rows);

	srcImg = rotateMat(srcImg, angle);

	LOGD("cropImage 2, cropImage w=%d, h=%d", srcImg.cols, srcImg.rows);

	// convert
	cv::Point2f src_vertices[4];
	src_vertices[0] = Point((float)x1, (float)y1);
	src_vertices[1] = Point((float)x2, (float)y2);
	src_vertices[2] = Point((float)x3, (float)y3);
	src_vertices[3] = Point((float)x4, (float)y4);

	cv::Point2f dst_vertices[4];
	dst_vertices[0] = Point(0, 0);
	dst_vertices[1] = Point(0, (float)inflateH-1);
	dst_vertices[2] = Point((float)inflateW-1, (float)inflateH-1);
	dst_vertices[3] = Point((float)inflateW-1, 0);

	Mat warpMatrix = getPerspectiveTransform(src_vertices, dst_vertices);

	LOGD("cropImage 2");

	// inflate.
	cv::Mat rotatedImg;
	warpPerspective(srcImg, rotatedImg, warpMatrix, Size(inflateW, inflateH));

	vector<int> compression_params;
	compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
	compression_params.push_back(70);

	try {
		imwrite(dstPath, rotatedImg, compression_params);
	}
	catch (char* b) {
		LOGE("cropImage imwrite exception");
		return -1;
	}

	LOGD("cropImage e");

	return 1;
}


//------------------------------------------------------//

int setEditBitmap(char* path)
{
	LOGD("setEditBitmap s");

	matEdit = imread(path, CV_LOAD_IMAGE_COLOR);   // Read the file

	if(! matEdit.data )                              // Check for invalid input
	{
		LOGE("setEditBitmap error");
		return -1;
	}

	LOGD("setEditBitmap e, size=%d, %d", matEdit.cols, matEdit.rows);

	return 1;
}

int setEditMode(int mode, char* savePath)
{
	LOGD("setEditMode s");

	if (! matEdit.data){
		LOGE("setEditMode error");
		return -1;
	}

	double alpha = 1.20;
	int beta = 10;

	Mat dst;

	switch(mode){
	case MODE_ORIGIN:
		dst = matEdit.clone();
		break;
	case MODE_AUTO:
		dst = magicMat(matEdit, alpha, beta);
		break;
	case MODE_MAGIC:
		alpha = 1.8;
		beta = 25;
		dst = magicMat(matEdit, alpha, beta);
		break;
	case MODE_LIGHT:
		alpha = 1.2;
		beta = 30;
		dst = magicMat(matEdit, alpha, beta);
		break;
	case MODE_GRAY:
		cvtColor(matEdit, dst, CV_BGR2GRAY);
		break;

	case MODE_BW:
		cv::cvtColor(matEdit, dst, CV_BGR2GRAY);
		cv::medianBlur(dst, dst, 2);
		cv::threshold(dst, dst, 128, 255, CV_THRESH_BINARY);
		cv::dilate(dst, dst, Mat(), Point(-1, -1), 2);
		break;

	default:
		LOGE("setEditMode invalid mode");
		return -1;
	}

	vector<int> compression_params;
	compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
	compression_params.push_back(70);

	try {
		imwrite(savePath, dst, compression_params);
	}
	catch (char* b) {
		LOGE("setEditMode imwrite exception");
		return -1;
	}


	LOGD("setEditMode e");

	return 1;
}
//------------------------------------------------------//
// first load
void setPrevImage(char* path, int* ppos)
{
	LOGD("setPrevImage s");

	ppos[0] = 0;

	Mat src = imread(path, CV_LOAD_IMAGE_COLOR);   // Read the file

	if(! src.data )                              // Check for invalid input
	{
		LOGE("setPrevImage error");
		return;
	}

	Mat dst;
	cv::flip(src, dst, 0);

	// extract contours
	cv::cvtColor(dst, dst, CV_BGRA2GRAY);

	if( dst.empty()){
		LOGE("setImage error!");
		return;
	}

	LOGI("setPrevImage 12");

	cv::medianBlur(dst, dst, 5);
	cv::threshold(dst, dst, 60, 255, CV_THRESH_BINARY);
	cv::dilate(dst, dst, Mat(), Point(-1, -1), 2);

	LOGD("setPrevImage 2 dst.cols=%d, dst.rows=%d", dst.cols, dst.rows);

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	cv::findContours( dst, contours, hierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE );

	if( contours.size() != 0 )
	{
		for(int idx = 0 ; idx >= 0; idx = hierarchy[idx][0] )
		{
			Rect contourRect = cv::boundingRect(contours[idx]);

			//LOGI("setImage get contour idx=%d, x=%d, y=%d, w=%d, h=%d",
			//			idx, contourRect.x, contourRect.y, contourRect.width, contourRect.height);

			if ( (contourRect.width > (dst.cols/2)) && (contourRect.height > (dst.rows/2)) )
			{
				//LOGD("setImage Mat get contour OK idx=%d, x=%d, y=%d, w=%d, h=%d",
				//		idx, contourRect.x, contourRect.y, contourRect.width, contourRect.height);

				ppos[0] = 1; // success
				ppos[1] = contourRect.x;
				ppos[2] = contourRect.y;
				ppos[3] = contourRect.width;
				ppos[4] = contourRect.height;
				return;
			}
		}
	}

	LOGD("setImage e");

	return;
}
