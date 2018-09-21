package com.ef;

import org.springframework.data.repository.CrudRepository;
import com.ef.LogEntry;

public interface LogEntryRepository extends CrudRepository<LogEntry, Integer> {

}
