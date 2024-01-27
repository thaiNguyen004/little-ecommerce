package thainguyen.service.size;

import thainguyen.domain.Size;
import thainguyen.service.generic.GenericService;

public interface SizeService extends GenericService<Size> {

    Size create(Size size);

    Size updateByPut(Long id, Size size);

    Size updateByPatch(Long id, Size size);
}

