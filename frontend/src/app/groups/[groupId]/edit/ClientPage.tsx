'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import AddressSearchModal from '@/components/groups/AddressSearchModal';
import KakaoMapSelector from '@/components/groups/KakaoMapSelector';

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

  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        console.log('Fetching group data for ID:', groupId); // 디버깅용
        const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
          headers: {
            Accept: 'application/json',
          },
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('Received data:', data); // 디버깅용

        if (data.isSuccess && data.data) {
          const group = data.data;
          setForm({
            name: group.name || '',
            province: group.province || '',
            city: group.city || '',
            town: group.town || '',
            description: group.description || '',
            maxRecruitCount: group.maxRecruitCount || 1,
            categoryName: group.categoryName || '',
            recruitStatus: group.recruitStatus || 'RECRUITING',
          });
        } else {
          throw new Error(data.message || '그룹 정보를 불러오는데 실패했습니다.');
        }
      } catch (error) {
        console.error('Error fetching group data:', error);
        alert(error instanceof Error ? error.message : '그룹 정보를 불러오는데 실패했습니다.');
        router.push('/groups');
      }
    };

    if (groupId) {
      fetchGroupData();
    }
  }, [groupId, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(form),
        credentials: 'include',
      });

      const data = await response.json();

      if (data.isSuccess) {
        router.push(`/groups/${groupId}`);
      } else {
        alert(data.message || '그룹 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to update group:', error);
      alert('그룹 수정에 실패했습니다.');
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

  return (
    <div className='container mx-auto px-4 py-8'>
      <h1 className='text-2xl font-bold mb-6'>모임 수정하기</h1>

      <div className='mb-6 space-x-4'>
        <button
          onClick={() => setShowAddressModal(true)}
          className='px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'
        >
          주소 검색으로 가져오기
        </button>
        <button
          onClick={() => setShowMapSelector(true)}
          className='px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600'
        >
          지도에서 주소 가져오기
        </button>
      </div>

      {form.province && (
        <div className='mb-6 p-4 bg-gray-100 rounded'>
          선택된 주소: {form.province} {form.city} {form.town}
        </div>
      )}

      <form onSubmit={handleSubmit} className='space-y-6'>
        <div>
          <label className='block mb-2'>모임 이름</label>
          <input
            type='text'
            value={form.name}
            onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
            className='w-full p-2 border rounded'
            required
          />
        </div>

        <div>
          <label className='block mb-2'>카테고리</label>
          <input
            type='text'
            value={form.categoryName}
            onChange={(e) => setForm((prev) => ({ ...prev, categoryName: e.target.value }))}
            className='w-full p-2 border rounded'
            required
          />
        </div>

        <div>
          <label className='block mb-2'>최대 인원</label>
          <input
            type='number'
            min='1'
            value={form.maxRecruitCount}
            onChange={(e) => setForm((prev) => ({ ...prev, maxRecruitCount: parseInt(e.target.value) }))}
            className='w-full p-2 border rounded'
            required
          />
        </div>

        <div>
          <label className='block mb-2'>모집 상태</label>
          <select
            value={form.recruitStatus}
            onChange={(e) => setForm((prev) => ({ ...prev, recruitStatus: e.target.value }))}
            className='w-full p-2 border rounded'
            required
          >
            <option value='RECRUITING'>모집 중</option>
            <option value='CLOSED'>모집 완료</option>
          </select>
        </div>

        <div>
          <label className='block mb-2'>설명</label>
          <textarea
            value={form.description}
            onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
            className='w-full p-2 border rounded h-32'
            required
          />
        </div>

        <button type='submit' className='w-full py-2 bg-blue-500 text-white rounded hover:bg-blue-600'>
          수정하기
        </button>
      </form>

      {showAddressModal && (
        <AddressSearchModal onClose={() => setShowAddressModal(false)} onSelect={handleAddressSelect} />
      )}

      {showMapSelector && (
        <KakaoMapSelector onClose={() => setShowMapSelector(false)} onSelect={handleMapAddressSelect} />
      )}
    </div>
  );
}
