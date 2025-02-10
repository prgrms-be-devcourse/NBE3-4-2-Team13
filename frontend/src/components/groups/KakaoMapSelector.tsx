'use client';

import { useEffect, useRef, useState } from 'react';

interface KakaoMapSelectorProps {
  onClose: () => void;
  onSelect: (address: { province: string; city: string; town: string }) => void;
}

interface Address {
  province: string;
  city: string;
  town: string;
}

export default function KakaoMapSelector({ onClose, onSelect }: KakaoMapSelectorProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const [selectedAddress, setSelectedAddress] = useState<Address | null>(null);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&libraries=services&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => {
        if (!mapRef.current) return;

        const options = {
          center: new window.kakao.maps.LatLng(37.5666805, 126.9784147),
          level: 7,
        };

        const map = new window.kakao.maps.Map(mapRef.current, options);
        const geocoder = new window.kakao.maps.services.Geocoder();

        map.addListener('click', function (mouseEvent: any) {
          const latlng = mouseEvent.latLng;

          geocoder.coord2Address(latlng.getLng(), latlng.getLat(), function (result: any, status: any) {
            if (status === window.kakao.maps.services.Status.OK) {
              if (result[0]?.address) {
                const addr = result[0].address;
                const address = {
                  province: addr.region_1depth_name,
                  city: addr.region_2depth_name,
                  town: addr.region_3depth_name,
                };
                console.log('Selected address:', address);
                setSelectedAddress(address);
              }
            }
          });
        });

        const mapTypeControl = new window.kakao.maps.MapTypeControl();
        map.addControl(mapTypeControl, window.kakao.maps.ControlPosition.TOPRIGHT);

        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
      });
    };

    document.head.appendChild(script);

    return () => {
      if (document.head.contains(script)) {
        document.head.removeChild(script);
      }
    };
  }, []);

  const handleSelect = () => {
    if (selectedAddress) {
      onSelect(selectedAddress);
    }
  };

  return (
    <div className='fixed inset-0 bg-black/50 flex items-center justify-center z-50'>
      <div className='bg-white dark:bg-gray-800 rounded-lg p-6 w-[800px] max-h-[90vh] overflow-y-auto'>
        <h2 className='text-xl font-bold mb-4 dark:text-white'>지도에서 위치 선택</h2>
        <p className='text-sm text-gray-600 dark:text-gray-300 mb-4'>지도를 클릭하여 위치를 선택해주세요.</p>

        <div ref={mapRef} className='w-full h-[400px] mb-4 rounded-lg' />

        {selectedAddress && (
          <div className='mb-4 p-4 bg-gray-100 dark:bg-gray-700 rounded'>
            <p className='text-gray-900 dark:text-white'>
              선택된 주소: {selectedAddress.province} {selectedAddress.city} {selectedAddress.town}
            </p>
          </div>
        )}

        <div className='flex justify-end space-x-4'>
          <button onClick={onClose} className='px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600'>
            취소
          </button>
          <button
            onClick={handleSelect}
            className='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed'
            disabled={!selectedAddress}
          >
            선택
          </button>
        </div>
      </div>
    </div>
  );
}
