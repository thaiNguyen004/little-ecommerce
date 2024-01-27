package thainguyen.service.size;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import thainguyen.data.SizeRepository;
import thainguyen.domain.Size;
import thainguyen.service.generic.GenericServiceImpl;

@Service
public class SizeServiceImpl extends GenericServiceImpl<Size> implements SizeService {

    private final SizeRepository repo;

    public SizeServiceImpl(EntityManager em, SizeRepository repo) {
        super(em, Size.class);
        this.repo = repo;
    }

    @Override
    public Size create(Size size) {
        return repo.save(size);
    }

    @Override
    public Size updateByPut(Long id, Size sizeChanged) {
        return repo.findById(id).map(size -> {
            sizeChanged.setId(id);
            sizeChanged.setVersion(size.getVersion());
            return repo.save(sizeChanged);
        }).orElseGet(() -> null);
    }

    @Override
    public Size updateByPatch(Long id, Size size) {
        return repo.findById(id).map(s -> {
            if (size.getName() != null) {
                s.setName(size.getName());
            }
            if (size.getChest() != null) {
                s.setChest(size.getChest());
            }
            if (size.getWidth() != null) {
                s.setWidth(size.getWidth());
            }
            if (size.getLength() != null) {
                s.setLength(size.getLength());
            }
            if (size.getBrand() != null) {
                s.setBrand(size.getBrand());
            }
            if (size.getCategory() != null) {
                s.setCategory(size.getCategory());
            }
            return repo.save(s);
        }).orElseGet(() -> null);
    }
}
