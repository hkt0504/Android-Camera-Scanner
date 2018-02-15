#include "common.h"
#include <android/bitmap.h>

#define MAX(a,b)  ((a > b) ? a : b)
#define MIN(a,b)  ((a > b) ? b : a)


// contrast : -50 ~ 50
// brightness : -50 ~ 50
// detail : 0 ~ 100
void adjustContrastBright(unsigned short* src, unsigned short* dst, int w, int h, int contrast, int brightness, int detail)
{
	 int i, x, y, j, k;

	 float kDetail = (600.f - (float)detail) / 500.f;
	 float c= kDetail * (100 + contrast)/100.0f;
	 brightness+=128;

	 LOGD("adjustContrastBright s, contrast=%d, brightness=%d, detail=%d, kDetail=%f", contrast, brightness, detail, kDetail);

	 unsigned char cTable[256], rbTable[32], gTable[64];
	 unsigned short valTable[65536];

	 for (i=0; i<256; i++)
	 {
		cTable[i] = MAX(0, MIN(255,(int)((i-128)*c + brightness + 0.5f)));
	 }

	 for (i=0; i<32; i++)
	 {
		rbTable[i] = cTable[i << 3] >> 3;
	 }

	 for (i=0; i<64; i++)
	 {
		gTable[i] = cTable[i << 2] >> 2;
	 }

	 int idx, idxTmp1, idxTmp2;
	 int val, valTmp1, valTmp2;

	 // create table.
	 for (i=0; i<32; i++){

		 idxTmp1 = i << 11;
		 valTmp1 = rbTable[i] << 11;

		 for(j=0; j<64; j++){
			 idxTmp2 = idxTmp1 | (j << 5);
			 valTmp2 = valTmp1 | (gTable[j] << 5);

			 for (k=0; k<32; k++){

				 valTable[idxTmp2 | k] = (valTmp2 | rbTable[k]);
			 }
		 }
	 }

	 idx = 0;
	 int size = w * h;

	 for(i=0; i<size; i++){
		dst[idx] = valTable[src[idx]];
		idx++;
	 }

	 LOGI("adjustContrastBright e");
}
