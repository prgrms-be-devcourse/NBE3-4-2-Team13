'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';

interface Address {
  address_name: string;
  region_1depth_name: string; // 시/도
  region_2depth_name: string; // 구/군
  region_3depth_name: string; // 동/읍/면
}

interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  data: {
    documents: T[];
  };
}

interface AddressSearchModalProps {
  onClose: () => void;
  onSelect: (address: { province: string; city: string; town: string }) => void;
}

export default function AddressSearchModal({ onClose, onSelect }: AddressSearchModalProps) {
  console.log('Environment variables:', {
    API_URL: process.env.NEXT_PUBLIC_API_URL,
  });

  const [keyword, setKeyword] = useState('');
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const searchAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyword.trim()) return;

    setIsLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(
        `http://localhost:8080/api/v1/proxy/kakao/region?keyword=${encodeURIComponent(keyword)}`,
        {
          method: 'GET',
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          credentials: 'include',
        }
      );

      // 401 Unauthorized나 다른 에러 응답 처리
      if (response.status === 401) {
        setError('인증이 만료되었습니다. 다시 로그인해주세요.');
        return;
      }

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || '주소 검색에 실패했습니다.');
      }

      const data = await response.json();
      console.log('Response data:', data); // 전체 응답 데이터
      console.log('Documents:', data.data?.documents); // documents 배열 확인

      if (data.isSuccess && data.data) {
        const documents = data.data.documents || [];
        const addressMap = new Map<string, Address>();

        // 모든 주소를 파싱
        documents.forEach((doc: any) => {
          const addressParts = doc.address_name.split(' ');
          if (addressParts.length < 2) return; // 시/도, 구/군 정보가 없으면 스킵

          // 구/군 단위 주소 추가 (예: "서울 서초구")
          const districtKey = `${addressParts[0]}-${addressParts[1]}`;
          if (!addressMap.has(districtKey)) {
            addressMap.set(districtKey, {
              address_name: `${addressParts[0]} ${addressParts[1]}`,
              region_1depth_name: addressParts[0],
              region_2depth_name: addressParts[1],
              region_3depth_name: '',
            });
          }

          // 동/읍/면 단위 주소 추가 (예: "서울 서초구 서초동")
          if (addressParts[2]) {
            const dongKey = `${addressParts[0]}-${addressParts[1]}-${addressParts[2]}`;
            if (!addressMap.has(dongKey)) {
              addressMap.set(dongKey, {
                address_name: `${addressParts[0]} ${addressParts[1]} ${addressParts[2]}`,
                region_1depth_name: addressParts[0],
                region_2depth_name: addressParts[1],
                region_3depth_name: addressParts[2],
              });
            }
          }
        });

        // Map을 배열로 변환하고 정렬
        const sortedAddresses = Array.from(addressMap.values()).sort((a, b) => {
          // 구/군 단위가 먼저 오도록
          if (a.region_3depth_name === '' && b.region_3depth_name !== '') return -1;
          if (a.region_3depth_name !== '' && b.region_3depth_name === '') return 1;
          // 동/읍/면 이름으로 정렬
          return a.region_3depth_name.localeCompare(b.region_3depth_name);
        });

        const addresses = sortedAddresses
          .map((doc: any) => {
            const addressParts = doc.address_name.split(' ');
            return {
              address_name: addressParts.slice(0, 3).join(' '),
              region_1depth_name: addressParts[0] || '', // 시/도
              region_2depth_name: addressParts[1] || '', // 구/군
              region_3depth_name: addressParts[2] || '', // 동/읍/면
            };
          })
          .filter((addr) => {
            // 시/도, 구/군, 동/읍/면이 모두 있는 주소만 포함
            return addr.region_1depth_name && addr.region_2depth_name && addr.region_3depth_name;
          });

        setAddresses(addresses);
      } else {
        setError('검색 결과가 없습니다.');
      }
    } catch (error) {
      console.error('Address search error:', error);
      setError(error instanceof Error ? error.message : '주소 검색 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (address: Address) => {
    onSelect({
      province: address.region_1depth_name,
      city: address.region_2depth_name,
      town: address.region_3depth_name,
    });
  };

  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-background rounded-lg p-6 w-full max-w-md shadow-lg border border-border'>
        <h2 className='text-xl font-bold mb-4 text-foreground'>주소 검색</h2>

        <form onSubmit={searchAddress} className='mb-4'>
          <div className='flex gap-2'>
            <input
              type='text'
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder='지역명을 입력하세요 (예: 강남구, 분당구)'
              className='flex-1 px-4 py-2 rounded-md bg-background border border-input text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring'
            />
            <Button type='submit' disabled={isLoading}>
              {isLoading ? '검색중...' : '검색'}
            </Button>
          </div>
        </form>

        {error && <div className='mb-4 p-3 bg-destructive/10 text-destructive rounded-md'>{error}</div>}

        <div className='max-h-96 overflow-y-auto rounded-md border border-border bg-background'>
          {addresses.map((address, index) => (
            <div
              key={index}
              onClick={() => handleSelect(address)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleSelect(address);
                }
              }}
              role='button'
              tabIndex={0}
              className='p-4 hover:bg-accent/50 cursor-pointer border-b border-border last:border-b-0 transition-colors'
            >
              <div className='text-foreground space-x-1'>
                <span className='font-medium text-primary'>{address.region_1depth_name}</span>
                <span className='text-muted-foreground'>{address.region_2depth_name}</span>
                {address.region_3depth_name && (
                  <span className='text-muted-foreground'> {address.region_3depth_name}</span>
                )}
              </div>
            </div>
          ))}
          {addresses.length === 0 && keyword && !isLoading && (
            <div className='text-center text-muted-foreground py-8'>검색 결과가 없습니다.</div>
          )}
        </div>

        <div className='mt-4 flex justify-end'>
          <Button variant='outline' onClick={onClose}>
            닫기
          </Button>
        </div>
      </div>
    </div>
  );
}
