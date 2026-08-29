import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import PaginationControls from './PaginationControls.vue';

const FontAwesomeStub = { template: '<span />' };

function mountPagination(props = {}) {
  return mount(PaginationControls, {
    props: {
      currentPage: 1,
      totalPages: 3,
      totalElements: 25,
      pageSize: 10,
      ...props
    },
    global: { stubs: { 'font-awesome-icon': FontAwesomeStub } }
  });
}

describe('PaginationControls', () => {
  it('shows the current result range and changes pages', async () => {
    const wrapper = mountPagination();

    expect(wrapper.text()).toContain('Exibindo 11–20 de 25 registros');
    await wrapper.get('[aria-label="Próxima página"]').trigger('click');

    expect(wrapper.emitted('update:currentPage')).toEqual([[2]]);
  });

  it('disables navigation at page boundaries', () => {
    const first = mountPagination({ currentPage: 0 });
    const last = mountPagination({ currentPage: 2 });

    expect(first.get('[aria-label="Página anterior"]').attributes('disabled')).toBeDefined();
    expect(last.get('[aria-label="Próxima página"]').attributes('disabled')).toBeDefined();
  });

  it('emits the selected page size as a number', async () => {
    const wrapper = mountPagination();

    await wrapper.get('[aria-label="Registros por página"]').setValue('30');

    expect(wrapper.emitted('update:pageSize')).toEqual([[30]]);
  });
});
