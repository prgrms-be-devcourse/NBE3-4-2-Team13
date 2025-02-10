'use client';

import { useState } from 'react';

interface AddressSearchModalProps {
  onClose: () => void;
  onSelect: (address: { province: string; city: string; town: string }) => void;
}

interface KakaoAddress {
  address_name: string;
  place_name: string;
  road_address_name: string;
  x: string;
  y: string;
}

interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  data: {
    documents: T[];
    meta: {
      total_count: number;
      pageable_count: number;
      is_end: boolean;
    };
  };
}

export default function AddressSearchModal({ onClose, onSelect }: AddressSearchModalProps) {
  const [keyword, setKeyword] = useState('');
  const [addresses, setAddresses] = useState<KakaoAddress[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const searchAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyword.trim()) return;

    setIsLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/proxy/kakao/region?keyword=${encodeURIComponent(keyword)}`,
        {
          headers: {
            Accept: 'application/json',
          },
        }
      );
      const data: ApiResponse<KakaoAddress> = await response.json();
      console.log('Search response:', data); // 디버깅용

      if (data.isSuccess && data.data?.documents) {
        // 주소 정보가 있는 결과만 필터링하고 중복 제거
        const uniqueAddresses = new Map();
        data.data.documents.forEach((doc) => {
          const addressParts = doc.address_name.split(' ');
          if (addressParts.length >= 3) {
            const key = `${addressParts[0]}-${addressParts[1]}-${addressParts[2]}`;
            if (!uniqueAddresses.has(key)) {
              uniqueAddresses.set(key, {
                ...doc,
                region_1depth_name: addressParts[0],
                region_2depth_name: addressParts[1],
                region_3depth_name: addressParts[2],
              });
            }
          }
        });
        setAddresses(Array.from(uniqueAddresses.values()));
      }
    } catch (error) {
      console.error('Failed to search address:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-white dark:bg-gray-800 rounded-lg p-6 w-[480px] max-h-[80vh] overflow-y-auto'>
        <div className='flex justify-between items-center mb-4'>
          <h2 className='text-xl font-bold text-gray-900 dark:text-white'>주소 검색</h2>
          <button
            onClick={onClose}
            className='text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
          >
            ✕
          </button>
        </div>

        <form onSubmit={searchAddress} className='mb-4'>
          <div className='flex gap-2'>
            <input
              type='text'
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder='지역명을 입력하세요 (예: 강남구, 분당구)'
              className='flex-1 px-4 py-2 border rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent'
            />
            <button
              type='submit'
              className='px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors disabled:bg-blue-300'
              disabled={isLoading}
            >
              {isLoading ? '검색중...' : '검색'}
            </button>
          </div>
        </form>

        <div className='space-y-2'>
          {addresses.map((item: any, index) => (
            <button
              key={index}
              onClick={() =>
                onSelect({
                  province: item.region_1depth_name,
                  city: item.region_2depth_name,
                  town: item.region_3depth_name,
                })
              }
              className='w-full text-left px-4 py-3 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors border border-gray-200 dark:border-gray-700'
            >
              <div className='text-gray-900 dark:text-white font-medium'>
                {item.region_1depth_name} {item.region_2depth_name} {item.region_3depth_name}
              </div>
              <div className='text-sm text-gray-500 dark:text-gray-400 mt-1'>{item.address_name}</div>
            </button>
          ))}
          {addresses.length === 0 && keyword && !isLoading && (
            <div className='text-center text-gray-500 dark:text-gray-400 py-4'>검색 결과가 없습니다.</div>
          )}
          {isLoading && <div className='text-center text-gray-500 dark:text-gray-400 py-4'>검색중...</div>}
        </div>
      </div>
    </div>
  );
}
