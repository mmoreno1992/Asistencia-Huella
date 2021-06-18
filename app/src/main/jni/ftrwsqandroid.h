/*
 *    API functions prototypes
 */
#ifdef __cplusplus
extern "C" { /* assume C declarations for C++ */
#endif

//----------------------------------------------------------------------------

typedef struct FTR_PACKED
{
	unsigned int Width;       // image Width
	unsigned int Height;      // image Height
	unsigned int DPI;         // resolution Dots per inch
	unsigned int RAW_size;    // size of RAW image
	unsigned int BMP_size;    // size of BMP image
	unsigned int WSQ_size;    // size of WSQ image
	float        Bitrate;  // compression
} FTRIMGPARMS, *LPFTRIMGPARMS;

/*
  ftrWSQ_GetImageParameters    - load WSQ parameters[Width,Height...] from WSQ image
  please call it before ftrWSQ_ToRawImage for allocate right size of memory for output file
  Args  
            ftrWSQbuf       [in]    - pointer to WSQ image
            ImPar.WSQ_size  [in]
            ImPar           [out]   - pointer to FTRIMGPARMS structure
  Return                       - TRUE is OK, FALSE - error
*/
int ftrWSQ_GetImageParameters(unsigned char *ftrWSQbuf, LPFTRIMGPARMS ftrImPar);
/*
  ftrWSQ_GetDQTTable       - load WSQ parameters[Width,Height,DQTTable,o_quant,...] from WSQ image
  for WSQ compliance only
  Args  
            ftrWSQbuf  [in]    - pointer to WSQ image
            ImPar.WSQ_size [in]
            ImPar      [out]   - pointer to FTRIMGPARMS structure
            DQTTable    [out]
            o_quant     [out]
            o_size      [out]
  Return                       - TRUE is OK, FALSE - error
*/
int ftrWSQ_GetDQTTable(unsigned char *ftrWSQbuf, LPFTRIMGPARMS ftrImPar, float *DQTTable, short *o_quant, int *o_size);
//--------------------------------------------------------------------------


/*
  WSQ_ToRawImage                - convert image from WSQ format þýRAW format
  Àðãóìåíòþý ftrWSQbuf  [in]    -  pointer to WSQ image
             ImPar.WSQ_size [in] - size of WSQ image
             ImPar      [out]   - pointer to FTRIMGPARMS structure
             RAWbuf     [out]   -  pointer to RAW image with size RAW_size after WSQ_GetImageParameters
  Return                        - TRUE is OK, FALSE - error
*/
int ftrWSQ_ToRawImage(unsigned char *ftrWSQbuf, LPFTRIMGPARMS ftrImPar, unsigned char *RAWbuf);
//--------------------------------------------------------------------------

#ifdef __cplusplus
};
#endif
