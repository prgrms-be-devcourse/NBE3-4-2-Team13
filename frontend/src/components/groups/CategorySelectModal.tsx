'use client';

import { useEffect, useState } from 'react';

interface Category {
  id: number;
  name: string;
}

interface CategoryPageDto {
  categories: Category[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
  pageSize: number;
}

interface CategorySelectModalProps {
  onClose: () => void;
  onSelect: (categoryName: string) => void;
}

export default function CategorySelectModal({ onClose, onSelect }: CategorySelectModalProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchCategories(0);
  }, []);

  const fetchCategories = async (page: number) => {
    setIsLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`http://localhost:8080/api/v1/admin/categories?page=${page}&size=10`, {
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('카테고리 목록을 불러오는데 실패했습니다.');
      }

      const data = await response.json();
      if (data.isSuccess) {
        const categoryData: CategoryPageDto = data.data;
        setCategories(categoryData.categories);
        setCurrentPage(categoryData.currentPage);
        setTotalPages(categoryData.totalPages);
      }
    } catch (error) {
      setError(error instanceof Error ? error.message : '카테고리 로딩 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePageChange = (page: number) => {
    fetchCategories(page);
  };

  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md'>
        <h2 className='text-xl font-bold mb-4 text-gray-900 dark:text-white'>카테고리 선택</h2>

        {error && (
          <div className='mb-4 p-3 bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300 rounded-md'>
            {error}
          </div>
        )}

        <div className='max-h-96 overflow-y-auto'>
          {isLoading ? (
            <div className='text-center py-4 text-gray-600 dark:text-gray-400'>로딩 중...</div>
          ) : (
            <div className='space-y-2'>
              {categories.map((category) => (
                <button
                  key={category.id}
                  onClick={() => onSelect(category.name)}
                  className='w-full p-3 text-left hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md transition-colors text-gray-900 dark:text-white'
                >
                  {category.name}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className='flex justify-center gap-2 mt-4'>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => handlePageChange(i)}
                className={`px-3 py-1 rounded ${
                  currentPage === i
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-200 text-gray-700 dark:bg-gray-700 dark:text-gray-300'
                }`}
              >
                {i + 1}
              </button>
            ))}
          </div>
        )}

        <div className='mt-4 flex justify-end'>
          <button
            onClick={onClose}
            className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600'
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}
