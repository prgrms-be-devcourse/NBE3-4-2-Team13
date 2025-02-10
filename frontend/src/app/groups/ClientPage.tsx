'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Pagination from '@/components/common/Pagination';
import AddressSearchModal from '@/components/groups/AddressSearchModal';

interface GroupListInfo {
  id: number;
  categoryName: string;
  name: string;
  province: string;
  city: string;
  town: string;
  recruitStatus: string;
  maxRecruitCount: number;
  currentMemberCount: number;
  createdAt: string;
}

interface SearchParams {
  categoryName?: string;
  keyword?: string;
  province?: string;
  city?: string;
  town?: string;
}

export default function ClientPage() {
  const router = useRouter();
  const [groups, setGroups] = useState<GroupListInfo[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchParams, setSearchParams] = useState<SearchParams>({});
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState<{
    province?: string;
    city?: string;
    town?: string;
  }>({});
  const [useAddressSearch, setUseAddressSearch] = useState(false);

  useEffect(() => {
    fetchGroups();
  }, [currentPage, searchParams]);

  const fetchGroups = async () => {
    try {
      const queryParams = new URLSearchParams({
        page: currentPage.toString(),
        size: '10',
        sort: 'createdAt,desc',
        ...(searchParams.categoryName && { categoryName: searchParams.categoryName }),
        ...(searchParams.keyword && { keyword: searchParams.keyword }),
        ...(searchParams.province && { province: searchParams.province }),
        ...(searchParams.city && { city: searchParams.city }),
        ...(searchParams.town && { town: searchParams.town }),
      });

      const response = await fetch(`http://localhost:8080/api/v1/groups?${queryParams}`, {
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();
      if (data.isSuccess) {
        setGroups(data.data.content);
        setTotalPages(data.data.totalPages);
      }
    } catch (error) {
      console.error('Failed to fetch groups:', error);
    }
  };

  const handleGroupClick = (groupId: number) => {
    console.log('Navigating to group:', groupId);
    router.push(`/groups/${groupId}`);
  };

  const handleCreateClick = () => {
    router.push('/groups/create');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchParams({
      categoryName: category || undefined,
      keyword: keyword || undefined,
      ...selectedAddress,
    });
    setCurrentPage(0);
  };

  const handleAddressSelect = (address: { province: string; city: string; town: string }) => {
    setSelectedAddress(address);
    setIsAddressModalOpen(false);
    setSearchParams({
      ...searchParams,
      ...address,
    });
  };

  return (
    <div className='container mx-auto px-4 py-8'>
      {/* 검색 섹션 */}
      <div className='mb-8'>
        <form onSubmit={handleSearch} className='flex gap-4 items-center'>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className='px-4 py-2 border rounded-md dark:bg-gray-800 dark:border-gray-700 dark:text-white min-w-[120px] focus:ring-2 focus:ring-blue-500 focus:border-transparent'
          >
            <option value=''>전체 카테고리</option>
            <option value='STUDY'>스터디</option>
            <option value='HOBBY'>취미</option>
            <option value='EXERCISE'>운동</option>
          </select>

          <div className='flex items-center gap-2 px-4 py-2 border rounded-md dark:bg-gray-800 dark:border-gray-700'>
            <input
              type='checkbox'
              id='useAddressSearch'
              checked={useAddressSearch}
              onChange={(e) => {
                setUseAddressSearch(e.target.checked);
                if (!e.target.checked) {
                  setSelectedAddress({});
                  setSearchParams({
                    ...searchParams,
                    province: undefined,
                    city: undefined,
                    town: undefined,
                  });
                }
              }}
              className='w-4 h-4 text-blue-500 border-gray-300 rounded focus:ring-blue-500'
            />
            <label htmlFor='useAddressSearch' className='text-gray-700 dark:text-gray-300'>
              주소 검색 사용
            </label>
          </div>

          {useAddressSearch && (
            <button
              type='button'
              onClick={() => setIsAddressModalOpen(true)}
              className='px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-colors'
            >
              주소 선택
            </button>
          )}

          <input
            type='text'
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder='그룹 이름 또는 설명 검색'
            className='flex-1 px-4 py-2 border rounded-md dark:bg-gray-800 dark:border-gray-700 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent'
          />

          <button
            type='submit'
            className='px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors'
          >
            검색
          </button>

          <button
            type='button'
            onClick={handleCreateClick}
            className='px-6 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition-colors'
          >
            모임 생성
          </button>
        </form>

        {useAddressSearch && selectedAddress.province && (
          <div className='mt-2 px-4 py-2 bg-gray-100 dark:bg-gray-700 rounded-md'>
            <span className='text-gray-600 dark:text-gray-300'>
              선택된 주소: {selectedAddress.province} {selectedAddress.city} {selectedAddress.town}
            </span>
          </div>
        )}
      </div>

      {/* 그룹 목록 */}
      <div className='space-y-4'>
        {groups.map((group) => (
          <div
            key={group.id}
            onClick={() => handleGroupClick(group.id)}
            className='bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer'
          >
            <div className='flex justify-between items-center mb-4'>
              <span className='bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-3 py-1 rounded-full text-sm font-medium'>
                {group.categoryName}
              </span>
              <span className='text-gray-600 dark:text-gray-400 text-sm'>
                생성일: {new Date(group.createdAt).toLocaleDateString()}
              </span>
            </div>

            <h3 className='text-xl font-bold text-gray-900 dark:text-white mb-4'>{group.name}</h3>

            <div className='flex justify-between items-center'>
              <div className='text-gray-600 dark:text-gray-400'>
                <span>
                  위치: {group.province} {group.city} {group.town}
                </span>
              </div>
              <div className='flex items-center space-x-4'>
                <span className='text-gray-600 dark:text-gray-400'>
                  멤버: {group.currentMemberCount}/{group.maxRecruitCount}
                </span>
                <span
                  className={`px-3 py-1 rounded-full text-sm font-medium ${
                    group.recruitStatus === 'RECRUITING'
                      ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                      : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'
                  }`}
                >
                  {group.recruitStatus === 'RECRUITING' ? '모집중' : '모집완료'}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 페이지네이션 */}
      <div className='mt-8'>
        <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} />
      </div>

      {/* 주소 검색 모달 */}
      {isAddressModalOpen && (
        <AddressSearchModal onClose={() => setIsAddressModalOpen(false)} onSelect={handleAddressSelect} />
      )}
    </div>
  );
}
