'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import AddressSearchModal from '@/components/groups/AddressSearchModal';
import KakaoMapSelector from '@/components/groups/KakaoMapSelector';
import CategorySelectModal from '@/components/groups/CategorySelectModal';

interface UpdateGroupForm {
  name: string;
  province: string;
  city: string;
  town: string;
  description: string;
  maxRecruitCount: number;
  categoryName: string;
  recruitStatus: string;
}

interface Props {
  groupId: string;
}

export default function ClientPage({ groupId }: Props) {
  const router = useRouter();
  const [form, setForm] = useState<UpdateGroupForm>({
    name: '',
    province: '',
    city: '',
    town: '',
    description: '',
    maxRecruitCount: 1,
    categoryName: '',
    recruitStatus: 'RECRUITING',
  });
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [showMapSelector, setShowMapSelector] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          throw new Error('로그인이 필요합니다.');
        }

        const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('모임 정보를 불러오는데 실패했습니다.');
        }

        const data = await response.json();
        console.log('Received data:', data); // 디버깅용

        if (data.isSuccess && data.data) {
          const groupData = data.data;
          setForm({
            name: groupData.name,
            province: groupData.province,
            city: groupData.city,
            town: groupData.town,
            description: groupData.description,
            maxRecruitCount: groupData.maxRecruitCount,
            categoryName: groupData.categoryName,
            recruitStatus: groupData.recruitStatus,
          });
        } else {
          throw new Error(data.message || '모임 정보를 불러오는데 실패했습니다.');
        }
      } catch (error) {
        console.error('Error:', error);
        setError(error instanceof Error ? error.message : '모임 정보를 불러오는데 실패했습니다.');
        router.push('/groups');
      }
    };

    if (groupId) {
      fetchGroupData();
    }
  }, [groupId, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(form),
        credentials: 'include',
      });

      const data = await response.json();

      if (data.isSuccess) {
        router.push(`/groups/${groupId}`);
      } else {
        setError(data.message || '모임 수정에 실패했습니다.');
      }
    } catch (error) {
      setError(error instanceof Error ? error.message : '모임 수정에 실패했습니다.');
    }
  };

  const handleAddressSelect = (address: { province: string; city: string; town: string }) => {
    setForm((prev) => ({ ...prev, ...address }));
    setShowAddressModal(false);
  };

  const handleMapAddressSelect = (address: { province: string; city: string; town: string }) => {
    setForm((prev) => ({ ...prev, ...address }));
    setShowMapSelector(false);
  };

  const handleCategorySelect = (categoryName: string) => {
    setForm((prev) => ({ ...prev, categoryName }));
    setShowCategoryModal(false);
  };

  return (
    <div className='container mx-auto px-4 py-8'>
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6'>
        <h1 className='text-2xl font-bold text-gray-900 dark:text-white mb-6'>모임 수정하기</h1>

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

          {/* 카테고리 */}
          <div>
            <label className='block mb-2 text-gray-900 dark:text-white'>카테고리</label>
            <div className='flex gap-2'>
              <input
                type='text'
                value={form.categoryName}
                readOnly
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

          {/* 모집 상태 */}
          <div>
            <label className='block mb-2 text-gray-900 dark:text-white'>모집 상태</label>
            <select
              value={form.recruitStatus}
              onChange={(e) => setForm((prev) => ({ ...prev, recruitStatus: e.target.value }))}
              className='w-full p-2 rounded-md bg-white border border-gray-300 text-gray-900 dark:bg-gray-800 dark:border-gray-600 dark:text-white'
              required
            >
              <option value='RECRUITING'>모집 중</option>
              <option value='CLOSED'>모집 완료</option>
            </select>
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
            className='w-full py-2 px-4 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors'
          >
            수정하기
          </button>
        </form>
      </div>

      {/* 모달들 */}
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
