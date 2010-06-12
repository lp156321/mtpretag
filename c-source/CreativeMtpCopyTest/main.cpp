//#include "stdafx.h"
#include "stdio.h"
#include "conio.h"
// WMDM includes
#include "mswmdm_i.c"
#include "mswmdm.h"
#include "sac.h"
#include "scclient.h"
int main(int argc, char* argv[])
{
  HRESULT hr;
  IComponentAuthenticate* pICompAuth;
  CSecureChannelClient *m_pSacClient = new CSecureChannelClient;
  IWMDeviceManager3* m_pIdvMgr = NULL;
  //these are generic keys
  BYTE abPVK[] = {0x00};
  BYTE abCert[] = {0x00};
  printf("MTP Transfer\n");
  printf("============\n");
  CoInitialize(NULL);
  // get an authentication interface
  hr = CoCreateInstance(CLSID_MediaDevMgr, NULL, CLSCTX_ALL ,IID_IComponentAuthenticate, (void **)&pICompAuth);
  if SUCCEEDED(hr)
  {
    // create a secure channel client certificate
    hr = m_pSacClient->SetCertificate(SAC_CERT_V1, (BYTE*) abCert, sizeof(abCert), (BYTE*) abPVK, sizeof(abPVK));
    if SUCCEEDED(hr)
    {
      // bind the authentication interface to the secure channel client
      m_pSacClient->SetInterface(pICompAuth);
      // trigger communication
      hr = m_pSacClient->Authenticate(SAC_PROTOCOL_V1);                   
      if SUCCEEDED(hr)
      {
        // get main interface to media device manager
        hr = pICompAuth->QueryInterface(IID_IWMDeviceManager2, (void**)&m_pIdvMgr);
        if SUCCEEDED(hr)
        {
          // we now have a media device manager interface...
          // enumerate devices...
          IWMDMEnumDevice *pIEnumDev;
          wchar_t pwsString[256];
          char ch[256];
          hr = m_pIdvMgr->EnumDevices2(&pIEnumDev);
          if SUCCEEDED(hr) {
            hr = pIEnumDev->Reset(); // Next will now return the first device
            if SUCCEEDED(hr) {
              printf("");
              IWMDMDevice3* pIDevice;
              unsigned long ulNumFetched;
              size_t ret;
              hr = pIEnumDev->Next(1, (IWMDMDevice **)&pIDevice, &ulNumFetched);
              while (SUCCEEDED(hr) && (hr != S_FALSE)) {
                // output device name
                hr = pIDevice->GetName(pwsString, 256);
                if SUCCEEDED(hr) {
                  wcstombs_s(&ret, ch, 256, pwsString,_TRUNCATE);
                  printf("Found device %s\n", ch);
                }
                // get storage info
                DWORD tempDW;
                pIDevice->GetType(&tempDW);
                if (tempDW & WMDM_DEVICE_TYPE_STORAGE) {
                  IWMDMEnumStorage *pIEnumStorage = NULL;
                  IWMDMEnumStorage *pIEnumFileStorage = NULL;
                  IWMDMEnumStorage *pIEnumFileStorageL2 = NULL;
                  IWMDMStorage *pIStorage = NULL;
                  IWMDMStorage3 *pIFileStorage = NULL;
                  IWMDMStorage3 *pIFileStorage_L2 = NULL;
                  _WAVEFORMATEX format;
                  hr = pIDevice->EnumStorage(&pIEnumStorage);
                  if SUCCEEDED(hr) {
                    pIEnumStorage->Reset();
                    hr = pIEnumStorage->Next(1, (IWMDMStorage **)&pIStorage, &ulNumFetched);
                    while (SUCCEEDED(hr) && (hr != S_FALSE)) {
                      hr = pIStorage->EnumStorage(&pIEnumFileStorage);
                      if SUCCEEDED(hr) {
                        pIEnumFileStorage->Reset();
                        hr = pIEnumFileStorage->Next(1, (IWMDMStorage **)&pIFileStorage, &ulNumFetched);
                        while (SUCCEEDED(hr) && hr != S_FALSE) {
                          hr = pIFileStorage->GetName(pwsString, 256);
                          hr = pIFileStorage->GetAttributes(&tempDW, &format);
                          if SUCCEEDED(hr) {
                            if (tempDW & WMDM_FILE_ATTR_FOLDER) {                             
                              if (wcscmp(pwsString, L"Music") == 0) {
                                IWMDMStorage3 *pNewStorage;
                                hr = pIFileStorage->QueryInterface(IID_IWMDMStorage3, (void **)&pNewStorage);
                                if SUCCEEDED(hr) {
                                  IWMDMStorageControl3 *pIWMDMStorageControl;
                                  hr = pNewStorage->QueryInterface(IID_IWMDMStorageControl3,
                                                   (void**)&pIWMDMStorageControl);
                                  if SUCCEEDED(hr)
                                  {
                                    IWMDMMetaData *pIWMDMMetaData = NULL;
                                    hr = pNewStorage->CreateEmptyMetadataObject(&pIWMDMMetaData);
                                    if (SUCCEEDED(hr)) {
                                      DWORD dw = WMDM_FORMATCODE_MP3;
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_DWORD, g_wszWMDMFormatCode, (BYTE *)&dw, sizeof(dw));
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_STRING, g_wszWMDMTitle, (BYTE *)L"TSOS-05-23-2006", 32);
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_STRING, g_wszWMDMAlbumTitle, (BYTE *)L"www.skinnyonsports.com", 46);
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_STRING, g_wszWMDMAuthor, (BYTE *)L"The Skinny on Sports", 42);
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_STRING, g_wszWMDMGenre, (BYTE *)L"Podcast", 16);
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_STRING, g_wszWMDMYear, (BYTE *)L"2006", 10);
                                      dw = 0;
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_DWORD, g_wszWMDMTrack, (BYTE *)&dw, sizeof(dw));
                                      DWORD ow[2];
                                      ow[0] = 0x77825f00;
                                      ow[1] = 1;
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_QWORD, g_wszWMDMDuration, (BYTE *)ow, 2 * sizeof(dw));
                                      ow[0] = 0x7532e5;
                                      ow[1] = 0;
                                      hr = pIWMDMMetaData->AddItem(WMDM_TYPE_QWORD, g_wszWMDMFileSize, (BYTE *)ow, 2 * sizeof(dw));
                                      if (SUCCEEDED(hr)) {
                                        IWMDMStorage *pNewObject = NULL;
                                        hr = pIWMDMStorageControl->Insert3(
                                             WMDM_MODE_BLOCK | WMDM_CONTENT_FILE,
                                             0,
                                             L"C:\\temp\\testfile.mp3",
                                             NULL,
                                             NULL,
                                             NULL,
                                             pIWMDMMetaData,
                                             NULL,
                                             (IWMDMStorage **)&pNewObject);
                                       
                                        if SUCCEEDED(hr)
                                          wprintf(L"Insert Succeeded\n");
                                      }
                                    }
                                  }
                                }
                              }
                               
                            } else {
                              printf("\n");
                            }
                          } else {
                            printf("\n");
                          }
                          pIFileStorage->Release();
                          hr = pIEnumFileStorage->Next(1, (IWMDMStorage **)&pIFileStorage, &ulNumFetched);
                        }
                        pIEnumFileStorage->Release();
                      }

                      hr = pIEnumStorage->Next(1, (IWMDMStorage **)&pIStorage, &ulNumFetched);
                    }
                    pIEnumStorage->Release();
                  }
                }
                // move to next device
                hr = pIEnumDev->Next(1, (IWMDMDevice **)&pIDevice, &ulNumFetched);
              }
              pIEnumDev->Release();
            }
            m_pIdvMgr->Release();
          }
          pICompAuth->Release();
        }
      }
    }
  }
  char c = _getch();
  CoUninitialize();
  return 0;
}