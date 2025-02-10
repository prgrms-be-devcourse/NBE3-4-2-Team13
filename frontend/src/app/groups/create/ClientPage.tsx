'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import AddressSearchModal from '@/components/groups/AddressSearchModal';
import KakaoMapSelector from '@/components/groups/KakaoMapSelector';
import CategorySelectModal from '@/components/groups/CategorySelectModal';

interface CreateGroupForm {
  name: string;
  province: string;
  city: string;
  town: string;
  description: string;
  maxRecruitCount: number;
  categoryName: string;
}

// 임시 카테고리 데이터 (나중에 서버에서 가져올 예정)
const TEMP_CATEGORIES = ['운동/스포츠', '게임', '독서', '음악', '영화', '요리', '여행', '프로그래밍'];

export default function ClientPage() {
  const router = useRouter();
  const [form, setForm] = useState<CreateGroupForm>({
    name: '',
    province: '',
    city: '',
    town: '',
    description: '',
    maxRecruitCount: 1,
    categoryName: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [showMapSelector, setShowMapSelector] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // 필수 필드 검증
    if (!form.province || !form.city || !form.town) {
      setError('주소를 선택해주세요.');
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      // form 데이터가 모두 포함되어 있는지 확인
      console.log('Submitting form:', form);

      const response = await fetch('http://localhost:8080/api/v1/groups', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: form.name,
          province: form.province,
          city: form.city,
          town: form.town,
          description: form.description,
          maxRecruitCount: form.maxRecruitCount,
          categoryName: form.categoryName,
        }),
        credentials: 'include',
      });

      const data = await response.json();

      if (data.isSuccess) {
        router.push('/groups');
      } else {
        setError(data.message || '모임 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to create group:', error);
      setError('모임 생성 중 오류가 발생했습니다.');
    }
  };

  const handleAddressSelect = (address: { province: string; city: string; town: string }) => {
    console.log('Address selected:', address); // 디버깅용
    setForm((prev) => ({
      ...prev,
      province: address.province,
      city: address.city,
      town: address.town,
    }));
    setShowAddressModal(false);
  };

  const handleMapAddressSelect = (address: { province: string; city: string; town: string }) => {
    console.log('Map address selected:', address); // 디버깅용
    setForm((prev) => ({
      ...prev,
      province: address.province,
      city: address.city,
      town: address.town,
    }));
    setShowMapSelector(false);
  };

  const handleCategorySelect = (categoryName: string) => {
    setForm((prev) => ({ ...prev, categoryName }));
    setShowCategoryModal(false);
  };

  return (
    <div className='container mx-auto px-4 py-8'>
      <h1 className='text-2xl font-bold mb-6 text-foreground'>모임 만들기</h1>

      {/* 주소 선택 버튼 */}
      <div className='mb-6 flex gap-4'>
        <button
          type='button'
          onClick={() => setShowAddressModal(true)}
          className='flex-1 px-4 py-2 bg-white text-gray-800 border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 transition-colors'
        >
          주소 검색으로 가져오기
        </button>
        <button
          type='button'
          onClick={() => setShowMapSelector(true)}
          className='flex-1 px-4 py-2 bg-white text-gray-800 border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 transition-colors'
        >
          지도에서 주소 가져오기
        </button>
      </div>

      {/* 선택된 주소 표시 */}
      {form.province && (
        <div className='mb-6 p-4 bg-gray-100 dark:bg-gray-800 rounded-lg'>
          <p className='text-gray-900 dark:text-white'>
            선택된 주소: {form.province} {form.city} {form.town}
          </p>
        </div>
      )}

      <form onSubmit={handleSubmit} className='space-y-6'>
        {/* 모임 이름 */}
        <div>
          <label className='block mb-2 text-gray-900 dark:text-white'>모임 이름</label>
          <input
            type='text'
            value={form.name}
            onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
            className='w-full p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
            required
          />
        </div>

        {/* 카테고리 선택 */}
        <div>
          <label className='block mb-2 text-gray-900 dark:text-white'>카테고리</label>
          <div className='flex gap-2'>
            <input
              type='text'
              value={form.categoryName}
              readOnly
              placeholder='카테고리를 선택하세요'
              className='flex-1 p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
            />
            <button
              type='button'
              onClick={() => setShowCategoryModal(true)}
              className='px-4 py-2 bg-white text-gray-800 border border-gray-300 rounded-md hover:bg-gray-50 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700'
            >
              선택
            </button>
          </div>
        </div>

        {/* 최대 인원 */}
        <div>
          <label className='block mb-2 text-gray-900 dark:text-white'>최대 인원</label>
          <input
            type='number'
            min='1'
            value={form.maxRecruitCount}
            onChange={(e) => setForm((prev) => ({ ...prev, maxRecruitCount: parseInt(e.target.value) }))}
            className='w-full p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
            required
          />
        </div>

        {/* 설명 */}
        <div>
          <label className='block mb-2 text-gray-900 dark:text-white'>설명</label>
          <textarea
            value={form.description}
            onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
            className='w-full p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white h-32'
            required
          />
        </div>

        {error && (
          <div className='p-4 bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300 rounded-md'>{error}</div>
        )}

        <button
          type='submit'
          className='w-full py-2 px-4 bg-blue-500 text-white rounded-md hover:bg-blue-600 dark:bg-blue-600 dark:hover:bg-blue-700 transition-colors'
        >
          모임 만들기
        </button>
      </form>

      {showAddressModal && (
        <AddressSearchModal onClose={() => setShowAddressModal(false)} onSelect={handleAddressSelect} />
      )}

      {showMapSelector && (
        <KakaoMapSelector onClose={() => setShowMapSelector(false)} onSelect={handleMapAddressSelect} />
      )}

      {showCategoryModal && (
        <CategorySelectModal onClose={() => setShowCategoryModal(false)} onSelect={handleCategorySelect} />
      )}
    </div>
  );
}
