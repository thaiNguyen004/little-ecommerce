package thainguyen.service.size;

import thainguyen.domain.Size;
import thainguyen.service.generic.GenericService;

public interface SizeService extends GenericService<Size> {

    Size create(Size size);

    Size updateSize(Long id, Size size);
}

