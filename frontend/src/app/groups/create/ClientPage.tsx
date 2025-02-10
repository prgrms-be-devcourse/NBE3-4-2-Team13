'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import AddressSearchModal from '@/components/groups/AddressSearchModal';
import KakaoMapSelector from '@/components/groups/KakaoMapSelector';

interface CreateGroupForm {
  name: string;
  province: string;
  city: string;
  town: string;
  description: string;
  maxRecruitCount: number;
  categoryName: string;
}

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

  const [showAddressModal, setShowAddressModal] = useState(false);
  const [showMapSelector, setShowMapSelector] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/v1/groups', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(form),
        credentials: 'include',
      });

      const data = await response.json();

      if (data.isSuccess) {
        router.push('/groups');
      } else {
        alert(data.message || '그룹 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to create group:', error);
      alert('그룹 생성에 실패했습니다.');
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
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6 dark:text-white">모임 만들기</h1>
      
      <div className="mb-6 space-x-4">
        <button
          onClick={() => setShowAddressModal(true)}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          주소 검색으로 가져오기
        </button>
        <button
          onClick={() => setShowMapSelector(true)}
          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          지도에서 주소 가져오기
        </button>
      </div>

      {form.province && (
        <div className="mb-6 p-4 bg-gray-100 dark:bg-gray-700 rounded">
          <p className="dark:text-white">
            선택된 주소: {form.province} {form.city} {form.town}
          </p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block mb-2 dark:text-white">모임 이름</label>
          <input
            type="text"
            value={form.name}
            onChange={(e) => setForm(prev => ({ ...prev, name: e.target.value }))}
            className="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            required
          />
        </div>

        <div>
          <label className="block mb-2 dark:text-white">카테고리</label>
          <input
            type="text"
            value={form.categoryName}
            onChange={(e) => setForm((prev) => ({ ...prev, categoryName: e.target.value }))}
            className="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            required
          />
        </div>

        <div>
          <label className="block mb-2 dark:text-white">최대 인원</label>
          <input
            type="number"
            min="1"
            value={form.maxRecruitCount}
            onChange={(e) => setForm((prev) => ({ ...prev, maxRecruitCount: parseInt(e.target.value) }))}
            className="w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            required
          />
        </div>

        <div>
          <label className="block mb-2 dark:text-white">설명</label>
          <textarea
            value={form.description}
            onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
            className="w-full p-2 border rounded h-32 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            required
          />
        </div>

        <button
          type="submit"
          className="w-full py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
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
    </div>
  );
}
