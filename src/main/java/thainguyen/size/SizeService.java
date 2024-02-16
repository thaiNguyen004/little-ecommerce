package thainguyen.size;

import thainguyen.generic.GenericService;

public interface SizeService extends GenericService<Size> {

    Size create(Size size);

    Size updateSize(Long id, Size size);
}

